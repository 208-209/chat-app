package controllers

import javax.inject._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.cache.SyncCacheApi
import play.api.mvc._

import scala.concurrent.duration._

import models._

case class ChannelForm(isPublic: Boolean, channelName: String, purpose: String, members: List[Long])

@Singleton
class ChannelController @Inject()(val cache: SyncCacheApi, cc: ControllerComponents) extends TwitterLoginController(cc) with I18nSupport {

  val channelForm = Form(
    mapping(
      "isPublic" -> boolean,
      "channelName" -> text(minLength = 3, maxLength = 30),
      "purpose" -> text(minLength = 3, maxLength = 55),
      "members" -> list(longNumber)
    )(ChannelForm.apply)(ChannelForm.unapply)
  )


  def read(channelId: String) = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>
    request.accessToken match {
      case Some(token) =>
        channelAndUserFindOne(channelId) match {
          case Some(channel) if isReadable(token, channel) =>
            val bundleData =  bundle(token, channel)
            Ok(views.html.channel(channelForm, bundleData._8)(bundleData._1, channel, bundleData._2, bundleData._3, bundleData._4, bundleData._5, bundleData._6, bundleData._7))
          case _ => NotFound("指定されたチャンネルは見つかりません")
        }
      case None =>
        // ログインできなかった際のリダイレクト機能
        val from = request.uri.split("/channels/").last
        if(from.matches("""[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}""")) // UUID
          cache.set("loginFrom", from, 10.minutes)
        Redirect(routes.OAuthController.login())
    }
  }


  def create(channelId: String) = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>
    request.accessToken match {
      case Some(token) =>
        channelForm.bindFromRequest.fold(
          error => { channelAndUserFindOne(channelId) match {
              case Some(channel) =>
                val bundleData = bundle(token, channel)
                BadRequest(views.html.channel(error, bundleData._8)(bundleData._1, channel, bundleData._2, bundleData._3, bundleData._4, bundleData._5, bundleData._6, bundleData._7))
              case None => NotFound("指定されたチャンネルは見つかりません")
            }
          },
          form => {
            val channelId = java.util.UUID.randomUUID().toString
            val channelName = form.channelName
            val description = form.purpose
            val isPublic = form.isPublic
            val members = (token.getUserId :: form.members).mkString(",") // チャンネル作成者は必ずメンバーになる
            val createdBy = token.getUserId
            val updatedAt = java.time.OffsetDateTime.now()
            channelInsert(Channel(channelId, channelName, description, isPublic, members, createdBy, updatedAt))
            Redirect(routes.ChannelController.read(channelId))
          }
        )
      case None => Redirect(routes.OAuthController.login())
    }
  }


  def update(channelId: String) = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>
    request.accessToken match {
      case Some(token) =>
        channelAndUserFindOne(channelId) match {
          case Some(channel) if isMineChannel(token, channel) =>
            channelForm.bindFromRequest.fold(
              error => {
                val bundleData = bundle(token, channel)
                BadRequest(views.html.channel(channelForm, error)(bundleData._1, channel, bundleData._2, bundleData._3, bundleData._4, bundleData._5, bundleData._6, bundleData._7))
              },
              form => {
                val editedChannel = channel._1.copy(
                  channelName = form.channelName,
                  purpose = form.purpose,
                  isPublic = form.isPublic,
                  members = (token.getUserId :: form.members).mkString(","),
                  updatedAt = java.time.OffsetDateTime.now()
                )
                channelUpdate(editedChannel)
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
      case Some(token) =>
        channelAndUserFindOne(channelId) match {
          case Some(channel) if isMineChannel(token, channel) =>
            deleteChannelAggregate(channelId)
            Redirect(routes.ChannelController.read("general"))
          case _ => NotFound("指定されたチャンネルがない、または、削除する権限がありません")
        }
      case None => Redirect(routes.OAuthController.login())
    }
  }

  /**
    * チャンネルページの観覧ができるか
    * パブリックチャンネル => 全員可
    * プライベートチャンネル => Channelのmembersに含まれるかどうか
    *
    * @param token
    * @param channel
    * @return
    */
  private def isReadable(token: twitter4j.auth.AccessToken, channel: (Channel, User)): Boolean = {
    channel._1.isPublic || channel._1.members.split(",").map(_.toLong).contains(token.getUserId)
  }

  /**
    * チャンネルの作成者とリクエストユーザーが一致するか
    * generalチャンネルの編集と削除はできない
    *
    * @param token
    * @param channel
    * @return
    */
  private def isMineChannel(token: twitter4j.auth.AccessToken, channel: (Channel, User)): Boolean = {
    channel._1.createdBy == token.getUserId && channel._1.channelId != "general"
  }

  /**
    * このuserIdがプライベートチャンネルにアクセスできるメンバーの一員であるか
    *
    * @param channel
    * @param user
    * @return
    */
  private def isMember(channel: Channel, userId: Long): Boolean = {
    channel.members.split(",").map(_.toLong).contains(userId)
  }


  /**
    * 各チャンネルに渡す情報をまとめた関数
    *
    * @param token
    * @param channel
    * @param request
    * @return リクエストユーザーの情報
    *         データベースに登録してあるユーザー
    *         リクエストユーザーがアクセスできるチャンネル
    *         リクエストユーザーがブックマーク(isBookmark == true)しているチャンネル
    *         リクエストユーザーのブックマークマップ（key: channelId, value: isBookmark）
    *         チャンネルのメッセージ
    *         メッセージを送るWebSocketのurl
    *         チャンネル情報が入った編集用のフォーム
    */
  private def bundle(token: twitter4j.auth.AccessToken, channel: (Channel, User))(implicit request: TwitterLoginRequest[AnyContent]): (Option[User], Seq[User], Seq[Channel], Seq[(Bookmark, Channel)], Map[String, Boolean], Seq[(Message, User)], String, Form[ChannelForm]) = {
    val user = userFindById(token.getUserId)
    val users = userFindAll()
    val channels = channelFindAll().filter { ch => ch.isPublic || isMember(ch, token.getUserId) }
    val bookmarks = bookmarkAndChannelFindAll(token.getUserId).filter{ case (bookmark, ch) => ch.isPublic || isMember(ch, bookmark.userId) }
    val bookmarkMap = createBookmarkMap(token.getUserId)
    val messages = messageFindAll(channel._1.channelId)
    val webSocketUrl = sys.env.get("HEROKU_URL") match {
      case Some(_) => routes.MessageController.socket(channel._1.channelId, token.getUserId).webSocketURL(secure = true)
      case None => routes.MessageController.socket(channel._1.channelId, token.getUserId).webSocketURL()
    }
    val members = channel._1.members.split(",").map(_.toLong).toList
    val editForm = channelForm.fill(ChannelForm(channel._1.isPublic, channel._1.channelName, channel._1.purpose, members))

    (user, users, channels, bookmarks, bookmarkMap, messages, webSocketUrl, editForm)
  }



}

