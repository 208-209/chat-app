package controllers

import javax.inject._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.cache.SyncCacheApi
import play.api.mvc._
import models._

case class ChannelForm(isPublic: Boolean, channelName: String, description: String, members: Seq[Long])

@Singleton
class ChannelController @Inject()(val cache: SyncCacheApi, cc: ControllerComponents) extends TwitterLoginController(cc) with I18nSupport {

  val channelForm = Form(
    mapping(
      "isPublic" -> boolean,
      "channelName" -> nonEmptyText(minLength = 3, maxLength = 20),
      "description" -> text(minLength = 3, maxLength = 255),
      "members" -> seq(longNumber)
    )(ChannelForm.apply)(ChannelForm.unapply)
  )

  def read(channelId: String) = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>
    request.accessToken match {
      case Some(accessToken) =>
        channelAndUserFindOne(channelId) match {
          case Some(channel) if isEnter(accessToken, channel) =>
            val bundleData =  bundle(accessToken, channel)
            Ok(views.html.channel(request.accessToken)(channelForm, bundleData._8)(channel, bundleData._1, bundleData._2, bundleData._3, bundleData._4, bundleData._5, bundleData._6, bundleData._7))
          case _ => NotFound("指定されたチャンネルは見つかりません。")
        }
      case None => Redirect(routes.OAuthController.login())
    }
  }

  def create(channelId: String) = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>

    request.accessToken match {
      case Some(accessToken) =>

        channelAndUserFindOne(channelId) match {
          case Some(channel) =>

            val bundleData = bundle(accessToken, channel)

            channelForm.bindFromRequest.fold(
              error => BadRequest(views.html.channel(request.accessToken)(error, bundleData._8)(channel, bundleData._1, bundleData._2, bundleData._3, bundleData._4, bundleData._5, bundleData._6, bundleData._7)),
              form => {
                println(form)

                val channelId = java.util.UUID.randomUUID().toString
                val channelName = form.channelName
                val description = form.description
                val isPublic = form.isPublic
                val members = if(form.members.isEmpty) accessToken.getUserId.toString else form.members.mkString(",")
                val createdBy = accessToken.getUserId
                val updatedAt = java.time.OffsetDateTime.now()

                channelInsert(Channel(channelId, channelName, description, isPublic, members, createdBy, updatedAt))

                Redirect(routes.ChannelController.read(channelId))
              }
            )

          case None => NotFound("指定されたチャンネルは見つかりません。")
        }

      case None => Redirect(routes.OAuthController.login())
    }
  }

  def update(channelId: String) = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>

    request.accessToken match {
      case Some(accessToken) =>

        channelAndUserFindOne(channelId) match {
          case Some(channel) if isMain(accessToken, channel) =>

            val bundleData = bundle(accessToken, channel)

            channelForm.bindFromRequest.fold(
              error => BadRequest(views.html.channel(request.accessToken)(error, bundleData._8)(channel, bundleData._1, bundleData._2, bundleData._3, bundleData._4, bundleData._5, bundleData._6, bundleData._7)),
              form => {
                val editChannel = Channel(
                  channelId = channel._1.channelId,
                  channelName = form.channelName,
                  description = form.description,
                  isPublic = form.isPublic,
                  members = if(form.members.isEmpty) accessToken.getUserId.toString else form.members.mkString(","),
                  createdBy = accessToken.getUserId,
                  updatedAt = java.time.OffsetDateTime.now()
                )

                channelUpsert(editChannel)
                Redirect(routes.ChannelController.read(channelId))
              }
            )

          case _ => NotFound("指定されたチャンネルがない、または、編集する権限がありません")
        }

      case None => Redirect(routes.OAuthController.login())
    }
  }

  def delete(channelId: String) = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>

    request.accessToken match {
      case Some(accessToken) =>
        channelAndUserFindOne(channelId) match {
          case Some(channel) if isMain(accessToken, channel) =>

            channelAndMessageDelete(channelId)
            Redirect(routes.ChannelController.read("general"))

          case _ => NotFound("指定されたチャンネルがない、または、削除する権限がありません")
        }

      case None => Redirect(routes.OAuthController.login())
    }
  }

  /**channel._1.members.map(',').map(_.toLong).contains(accessToken.getUserId)
    * チャンネルを観覧できるかとうか
    *
    * パブリックか
    * メンバーの一員か
    *
    * @param accessToken
    * @param channel
    * @return
    */
  private def isEnter(accessToken: twitter4j.auth.AccessToken, channel: (Channel, User)): Boolean = {
    channel._1.isPublic || channel._1.members.split(",").map(_.toLong).contains(accessToken.getUserId)
  }


  /**
    * リクエストユーザーがチャンネルの製作者かどうか
    * あと、generalは編集と削除ができないように
    * @param accessToken
    * @param channel
    * @return
    */
  private def isMain(accessToken: twitter4j.auth.AccessToken, channel: (Channel, User)): Boolean = {
    channel._1.createdBy == accessToken.getUserId && channel._1.channelId != "general"
  }

  /**
    *
    * @param accessToken
    * @param channel
    * @return
    */
  private def bundle(accessToken: twitter4j.auth.AccessToken, channel: (Channel, User))(implicit request: TwitterLoginRequest[AnyContent]): (Seq[Channel], Seq[User], Map[Long, String], Seq[(Bookmark, Channel)], Map[String, Boolean], Seq[(Message, User)], String, Form[ChannelForm]) = {

    val channels = channelFindAll.filter(channel => channel.isPublic || channel.members.split(",").map(_.toLong).contains(accessToken.getUserId))
    val bookmarks = bookmarkAndChannelFindAll(accessToken.getUserId).filter{ case (bookmark, channel) => channel.isPublic || channel.members.split(",").map(_.toLong).contains(bookmark.userId) }
    val webSocketUrl = sys.env.get("HEROKU_URL") match {
      case Some(_) => routes.MessageController.socket(channel._1.channelId, accessToken.getUserId).webSocketURL(secure = true)
      case None => routes.MessageController.socket(channel._1.channelId, accessToken.getUserId).webSocketURL()
    }
    val members = channel._1.members.split(",").map(_.toLong).toSeq
    val editForm = channelForm.fill(ChannelForm(channel._1.isPublic, channel._1.channelName, channel._1.description, members))

    (channels, userFindAll(), userMap(), bookmarks, bookmarkMap(accessToken.getUserId), messageFindAll(channel._1.channelId), webSocketUrl, editForm)
  }


}

