package controllers

import javax.inject._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.cache.SyncCacheApi
import play.api.mvc._
import play.api.libs.json._
import models._

case class ChannelForm(channelName: String, description: String)

@Singleton
class ChannelController @Inject()(val cache: SyncCacheApi, cc: ControllerComponents) extends TwitterLoginController(cc) with I18nSupport {

  val channelForm = Form(
    mapping(
      "channelName" -> nonEmptyText,
      "description" -> text
    )(ChannelForm.apply)(ChannelForm.unapply)
  )

  def read(channelId: String) = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>

    request.accessToken match {
      case Some(_) =>

        ChannelRepository.findOne(channelId) match {
          case Some(channel) =>
            val channels = ChannelRepository.findAll()
            val messages = MessageRepository.findAll(channelId)

            val editForm = channelForm.fill(ChannelForm(channel._1.channelName, channel._1.description))
            Ok(views.html.channel(request.accessToken)(channelForm, editForm)(channel, channels, messages))

          case None => NotFound("指定されたチャンネルは見つかりません。")
        }

      case None => Redirect(routes.OAuthController.login())
    }

  }

  def create(channelId: String) = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>

    request.accessToken match {
      case Some(_) =>

        ChannelRepository.findOne(channelId) match {
          case Some(channel) =>
            val channels = ChannelRepository.findAll()
            val messages = MessageRepository.findAll(channelId)

            val editForm = channelForm.fill(ChannelForm(channel._1.channelName, channel._1.description))
            channelForm.bindFromRequest.fold(
              error => BadRequest(views.html.channel(request.accessToken)(error, editForm)(channel, channels, messages)),
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


    val channelName = request.body.asFormUrlEncoded.get("channelName").head.toString.slice(0, 255)
    val description = request.body.asFormUrlEncoded.get("description").head.toString.slice(0, 255)
    val createdBy = request.body.asFormUrlEncoded.get("userId").head.toLong
    val updatedAt = java.time.OffsetDateTime.now()

    println(request.body)
    println(description)



    request.accessToken match {
      case Some(accessToken) if accessToken.getUserId == createdBy =>

        val channel = Channel(channelId, channelName, description, Some(createdBy), updatedAt)

        ChannelRepository.upsert(channel)



        val result = s"""{"channelName": "${channelName}", "description": "${description}", "updatedAt": "${updatedAt}"}"""

        Ok(Json.parse(result))
      case None => Redirect(routes.OAuthController.login())
    }


  }

}

