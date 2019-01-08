package controllers

import java.net.URL

import play.api.mvc._
import play.api.libs.streams.ActorFlow
import play.api.cache.SyncCacheApi
import play.api.libs.json._
import javax.inject._
import akka.actor._
import akka.stream.Materializer
import twitter4j.auth.AccessToken
import models._

import scala.concurrent.Future

class WaitingRoom() {
  var actorSet = Set[ActorRef]()
  var userNameSet = Set[String]()
}

@Singleton
class MessageController @Inject() (val cache: SyncCacheApi, cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends TwitterLoginController(cc) {

  var roomMap = Map[String, WaitingRoom]() // key: channelId, value: WaitingRoom

  def isSameOrigin(request: RequestHeader): Boolean = {
    request.headers.get("Origin") match {
      case Some(originValue) =>
        val url = new URL(originValue)

        println("url : " + url)
        println("url host : " + url.getHost)
        println("url.toString : " + url.toString)
        println("port : " + sys.env.getOrElse("PORT", ""))

        sys.env.get("HEROKU_URL") match {
          case Some(_) if url.toString == "https://play-chat-app.herokuapp.com" => true
          case None if url.toString == "http://localhost:9000" => true
          case _ => false
        }
      case _ => false
    }
  }

  def socket(channelId: String, userId: Long) = WebSocket.acceptOrResult[JsValue, JsValue] { request =>
    val sessionIdOpt = request.cookies.get(sessionIdName).map(_.value)
    val accessToken = sessionIdOpt.flatMap(cache.get[AccessToken])

    Future.successful(accessToken match {
      case Some(token) if token.getUserId == userId && isSameOrigin(request) =>
        Right(ActorFlow.actorRef { out => MyWebSocketActor.props(out, channelId, userId, token.getScreenName)})
      case _ => Left(Forbidden)
    })
  }

  object MyWebSocketActor {
    def props(out: ActorRef, channelId: String, userId: Long, userName: String) = Props(new MyWebSocketActor(out, channelId, userId, userName))
  }

  class MyWebSocketActor(out: ActorRef, channelId: String, userId: Long, userName: String) extends Actor {


    val myRoom = roomMap.get(channelId) match {
      case Some(room) => room
      case None =>
        val room = new WaitingRoom()
        roomMap = roomMap + (channelId -> room)
        println(room) // TODO
        println(roomMap) // TODO
        room
    }

    def receive = {
      case msg: JsValue =>

        val messageId = java.util.UUID.randomUUID().toString
        val message = (msg \ "message").as[String]
        val updatedAt = java.time.OffsetDateTime.now()

        MessageRepository.insert(Message(messageId, message, channelId, Some(userId), updatedAt))

        println(msg("message").getClass) // TODO
        println((msg \ "message").as[String].getClass) // TODO

        val msgJson = s"""{"messageId": "${messageId}", "message": "${message}", "updatedAt": "${updatedAt}"}"""


        myRoom.actorSet.foreach { out =>
          out ! Json.parse(msgJson)
        }
    }

    override def preStart(): Unit = {
      myRoom.actorSet = myRoom.actorSet + out
      myRoom.userNameSet = myRoom.userNameSet + userName
      println(myRoom.userNameSet)

      myRoom.actorSet.foreach { out =>
        val members = s"""{"members": "${myRoom.userNameSet.mkString(",")}"}"""
        out ! Json.parse(members)
      }

    }

    override def postStop(): Unit = {
      myRoom.actorSet = myRoom.actorSet - out
      myRoom.userNameSet = myRoom.userNameSet - userName

      myRoom.actorSet.foreach { out =>
        val members = s"""{"members": "${myRoom.userNameSet.mkString(",")}"}"""
        out ! Json.parse(members)
      }

    }

  }

}
