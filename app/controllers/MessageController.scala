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
import scala.concurrent.Future

class Room {
  var actorSet: Set[ActorRef] = Set.empty
  var userSet: Set[Long] = Set.empty
}

@Singleton
class MessageController @Inject() (val cache: SyncCacheApi, cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends TwitterLoginController(cc) {

  var roomMap: Map[String, Room] = Map.empty // key: channelId, value: Room

  def socket(channelId: String, userId: Long) = WebSocket.acceptOrResult[JsValue, JsValue] { request =>
    val sessionIdOpt = request.cookies.get(sessionIdName).map(_.value)
    val accessToken = sessionIdOpt.flatMap(cache.get[AccessToken])

    // 同一生成元ポリシー && パスのチャンネルが存在する && パスのユーザーIDとリクエストユーザーのIDが一致
    Future.successful(accessToken match {
      case Some(token) if isSameOrigin(request) && channelFindById(channelId).isDefined && userId == token.getUserId =>
        val profileImageUrl =  userFindById(userId).map(_.profileImageUrl).getOrElse("")
        Right(ActorFlow.actorRef { out => MyWebSocketActor.props(out, request, channelId, userId, token.getScreenName, profileImageUrl)})
      case _ => Left(Forbidden)
    })
  }


  object MyWebSocketActor {
    def props(out: ActorRef, request: RequestHeader, channelId: String, userId: Long, userName: String, profileImageUrl: String) = Props(new MyWebSocketActor(out, request, channelId, userId, userName, profileImageUrl))
  }

  class MyWebSocketActor(out: ActorRef, request: RequestHeader, channelId: String, userId: Long, userName: String, profileImageUrl: String) extends Actor {

    val myRoom = roomMap.get(channelId) match {
      case Some(room) => room
      case None =>
        val room = new Room()
        roomMap = roomMap + (channelId -> room)
        room
    }

    def receive = {
      case msg: JsValue =>

        // メッセージの投稿
        (msg \ "message").asOpt[String].foreach { message =>
          val messageId = java.util.UUID.randomUUID().toString
          val updatedAt = java.time.OffsetDateTime.now()

          messageInsert(Message(messageId, message, channelId, userId, updatedAt))
          val formattedUpdatedAt = updatedAt.format(messageFormatter)
          val result = Json.obj("messageId" -> messageId, "message" -> message, "createdBy" -> userId.toString, "userName" -> userName, "profileImageUrl" -> profileImageUrl, "updatedAt" -> formattedUpdatedAt)

          myRoom.actorSet.foreach { out => out ! result }
          println(
            s"""
               |[メッセージが投稿されました]
               |userId: $userId, userName: $userName
               |channelId: $channelId, messageId: $messageId
               |message: $message
               |remoteAddress: ${request.remoteAddress}
               |userAgent: ${request.headers.get("user-agent")}
            """.stripMargin)
        }

        // メッセージの削除
        (msg \ "delete").asOpt[String].foreach { messageId =>
          messageFindById(messageId).foreach { case message if message.createdBy == userId || isAdmin(userId) =>
            deleteMessage(message.messageId)
            val result = Json.obj("delete" -> messageId)

            myRoom.actorSet.foreach { out => out ! result }
            println(
              s"""
                 |[メッセージが削除されました]
                 |userId: $userId, userName: $userName
                 |channelId: $channelId, messageId: $messageId
                 |message: ${message.message}
                 |remoteAddress: ${request.remoteAddress}
                 |userAgent: ${request.headers.get("user-agent")}
              """.stripMargin)
          }
        }
    }

    override def preStart(): Unit = {
      myRoom.actorSet = myRoom.actorSet + out
      myRoom.userSet = myRoom.userSet + userId

      sendLoginUser(roomMap)
    }

    override def postStop(): Unit = {
      myRoom.actorSet = myRoom.actorSet - out
      myRoom.userSet = myRoom.userSet - userId

      sendLoginUser(roomMap)
    }
  }

  /**
    * コンテンツが同一の生成元から提供されているかを確認(スキーム・ホスト・ポート)
    * @param request
    * @return 真偽値
    */
  private[this] def isSameOrigin(request: RequestHeader): Boolean = {
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

  /**
    * ログインユーザーのuserIdを送信する
    * いずれかのチャンネルにアクセスしていれば、すべてのチャンネルに表示される
    * @param roomMap key: channelId, value: Room
    */
  private[this] def sendLoginUser(roomMap: Map[String, Room]): Unit = {
    val allActor = roomMap.flatMap { case (k, v) => v.actorSet }.toSet
    val allLoginUser = roomMap.flatMap { case (k, v) => v.userSet }.toSet
    val result = Json.obj("members" -> allLoginUser.map(_.toString))

    allActor.foreach { out => out ! result }
  }

}
