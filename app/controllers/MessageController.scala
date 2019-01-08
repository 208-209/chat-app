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

  def sameOriginCheck(request: RequestHeader): Boolean = {
    request.headers.get("Origin") match {
      case Some(originValue) if originMatches(originValue) => true
      case _ => false
    }
  }

  def originMatches(origin: String): Boolean = {
    val url = new URL(origin)
    println("url : " + url)
    println("url host : " + url.getHost)

    sys.env.get("HEROKU_URL") match {
      case Some(_) => true
      case None if url.getHost == "localhost" && url.getPort == 9000 => true
      case _ => false
    }
  }

  def socket(channelId: String, userId: Long) = WebSocket.acceptOrResult[JsValue, JsValue] { request =>

    val sessionIdOpt = request.cookies.get(sessionIdName).map(_.value)
    val accessToken = sessionIdOpt.flatMap(cache.get[AccessToken])


    Future.successful(accessToken match {
      case Some(token) if token.getUserId == userId && sameOriginCheck(request) =>

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
        println(room)
        println(roomMap)
        room
    }

    def receive = {
      case msg: JsValue =>

        val messageId = java.util.UUID.randomUUID().toString
        val message = (msg \ "message").as[String]
        val updatedAt = java.time.OffsetDateTime.now()

        MessageRepository.insert(Message(messageId, message, channelId, Some(userId), updatedAt))

        println(msg("message").getClass)
        println((msg \ "message").as[String].getClass)

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
