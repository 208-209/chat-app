package controllers

import play.api.mvc._
import play.api.libs.streams.ActorFlow
import play.api.cache.SyncCacheApi
import play.api.libs.json._
import javax.inject._
import akka.actor._
import akka.stream.Materializer
import twitter4j.auth.AccessToken
import models._
import java.net.URL
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class WaitingRoom() {
  var actorSet = Set[ActorRef]()
  var userNameSet = Set[Long]()
}

@Singleton
class MessageController @Inject() (val cache: SyncCacheApi, cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends TwitterLoginController(cc) {

  var roomMap = Map[String, WaitingRoom]() // key: channelId, value: WaitingRoom


  def socket(channelId: String, userId: Long) = WebSocket.acceptOrResult[JsValue, JsValue] { request =>

    val sessionIdOpt = request.cookies.get(sessionIdName).map(_.value)
    val accessToken = sessionIdOpt.flatMap(cache.get[AccessToken])

    // パスのチャンネルが存在する かつ パスのユーザーIDとリクエスト者のIDが一致する かつ 同一生成元ポリシー
    Future.successful(accessToken match {
      case Some(token) if channelFindById(channelId).isDefined && userId == token.getUserId && isSameOrigin(request) =>
        Right(ActorFlow.actorRef { out => MyWebSocketActor.props(out, channelId, userId, token.getScreenName)})
      case _ => Left(Forbidden)
    })
  }

  private def isSameOrigin(request: RequestHeader): Boolean = {
    request.headers.get("Origin") match {
      case Some(originValue) =>
        val url = new URL(originValue)
        sys.env.get("HEROKU_URL") match {
          case Some(_) if url.toString == "https://play-chat-app.herokuapp.com" => true
          case None if url.toString == "http://localhost:9000" => true
          case _ => false
        }
      case _ => false
    }
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

        // メッセージの投稿
        (msg \ "message").asOpt[String].map { message =>

          println("message : " + message)


        }

        // メッセージの削除
        (msg \ "deleteId").asOpt[String].map { deleteId =>

          println("message : " + deleteId)

        }





        val messageId = java.util.UUID.randomUUID().toString
        val message = (msg \ "message").as[String]
        val updatedAt = java.time.OffsetDateTime.now()

        MessageRepository.insert(Message(messageId, message, channelId, userId, updatedAt))

        val formattedUpdatedAt = updatedAt.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))

        println(msg("message").getClass)
        println((msg \ "message").as[String].getClass)

        val msgJson = s"""{"messageId": "${messageId}", "message": "${message}", "updatedAt": "${formattedUpdatedAt}", "userName": "${userName}"}"""


        myRoom.actorSet.foreach { out =>
          out ! Json.parse(msgJson)
        }
    }

    override def preStart(): Unit = {
      myRoom.actorSet = myRoom.actorSet + out
      myRoom.userNameSet = myRoom.userNameSet + userId
      println(myRoom.userNameSet)

      myRoom.actorSet.foreach { out =>
        val members = s"""{"members": "${myRoom.userNameSet.mkString(",")}"}"""
        out ! Json.parse(members)
      }


    }

    override def postStop(): Unit = {
      myRoom.actorSet = myRoom.actorSet - out
      myRoom.userNameSet = myRoom.userNameSet - userId

      myRoom.actorSet.foreach { out =>
        val members = s"""{"members": "${myRoom.userNameSet.mkString(",")}"}"""
        out ! Json.parse(members)
      }

    }




  }

}
