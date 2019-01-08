package controllers

import javax.inject._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.cache.SyncCacheApi
import play.api.mvc._
import play.api.libs.json._
import models._
import play.Play

case class ChannelForm(channelName: String, description: String)

@Singleton
class ChannelController @Inject()(val cache: SyncCacheApi, cc: ControllerComponents) extends TwitterLoginController(cc) with I18nSupport {

  val channelForm = Form(
    mapping(
      "channelName" -> nonEmptyText(minLength = 3, maxLength = 20),
      "description" -> text(minLength = 3, maxLength = 255)
    )(ChannelForm.apply)(ChannelForm.unapply)
  )

  def read(channelId: String) = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>




    request.accessToken match {
      case Some(accessToken) =>

        channelAndUserFindOne(channelId) match {
          case Some(channel) =>
            val channels = ChannelRepository.findAll()
            val messages = MessageRepository.findAll(channelId)
            val editForm = channelForm.fill(ChannelForm(channel._1.channelName, channel._1.description))
            val webSocketUrl =
              sys.env.get("HEROKU_URL") match {
                case Some(_) => routes.MessageController.socket(channelId, accessToken.getUserId).webSocketURL(secure = true)
                case None => routes.MessageController.socket(channelId, accessToken.getUserId).webSocketURL()
              }
            println(webSocketUrl)

            Ok(views.html.channel(request.accessToken)(channelForm, editForm)(channel, channels, messages, webSocketUrl))

          case None => NotFound("指定されたチャンネルは見つかりません。")
        }

      case None => Redirect(routes.OAuthController.login())
    }

  }

  def create(channelId: String) = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>

    request.accessToken match {
      case Some(accessToken) =>

        channelAndUserFindOne(channelId) match {
          case Some(channel) =>
            val channels = ChannelRepository.findAll()
            val messages = MessageRepository.findAll(channelId)
            val editForm = channelForm.fill(ChannelForm(channel._1.channelName, channel._1.description))
            val webSocketUrl =
              sys.env.get("HEROKU_URL") match {
                case Some(_) => routes.MessageController.socket(channelId, accessToken.getUserId).webSocketURL(secure = true)
                case None => routes.MessageController.socket(channelId, accessToken.getUserId).webSocketURL()
              }


            channelForm.bindFromRequest.fold(
              error => BadRequest(views.html.channel(request.accessToken)(error, editForm)(channel, channels, messages, webSocketUrl)),
              form => {
                val channelId = java.util.UUID.randomUUID().toString
                val channelName = form.channelName
                val description = form.description
                val createdBy = request.accessToken.map(_.getUserId)
                val updatedAt = java.time.OffsetDateTime.now()

                ChannelRepository.insert(Channel(channelId, channelName, description, createdBy, updatedAt))

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
          case Some(channel) if channel._1.createdBy == request.accessToken.map(_.getUserId) && channel._1.channelId != "general" =>

            val channels = ChannelRepository.findAll()
            val messages = MessageRepository.findAll(channelId)
            val editForm = channelForm.fill(ChannelForm(channel._1.channelName, channel._1.description))
            val webSocketUrl =
              sys.env.get("HEROKU_URL") match {
                case Some(_) => routes.MessageController.socket(channelId, accessToken.getUserId).webSocketURL(secure = true)
                case None => routes.MessageController.socket(channelId, accessToken.getUserId).webSocketURL()
              }


            channelForm.bindFromRequest.fold(
              error => BadRequest(views.html.channel(request.accessToken)(error, editForm)(channel, channels, messages, webSocketUrl)),
              form => {
                val editChannel = Channel(
                  channelId = channel._1.channelId,
                  channelName = form.channelName,
                  description = form.description,
                  createdBy = request.accessToken.map(_.getUserId),
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
      case Some(_) =>
        channelFindOne(channelId) match {
          case Some(channel) if channel.createdBy == request.accessToken.map(_.getUserId) && channel.channelId != "general" =>

            channelAndMessageDelete(channelId)
            Redirect(routes.ChannelController.read("general"))

          case _ => NotFound("指定されたチャンネルがない、または、削除する権限がありません")
        }

      case None => Redirect(routes.OAuthController.login())
    }
  }

}

