package controllers

import play.api.mvc._
import play.api.libs.streams.ActorFlow
import javax.inject.Inject
import akka.actor._
import akka.stream.Materializer
import javax.inject._
import play.api.cache.SyncCacheApi
import twitter4j.auth.AccessToken

import scala.concurrent.Future

class WaitingRoom() {
  var actorSet = Set[ActorRef]()
  var userNameSet = Set[String]()
}

@Singleton
class MessageController @Inject() (val cache: SyncCacheApi, cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends TwitterLoginController(cc) {

  var roomMap = Map[String, WaitingRoom]() // key: channelId, value: WaitingRoom

  def socket(channelId: String, userId: Long) = WebSocket.acceptOrResult[String, String] { request =>

    val sessionIdOpt = request.cookies.get(sessionIdName).map(_.value)
    val accessToken = sessionIdOpt.flatMap(cache.get[AccessToken])

    Future.successful(accessToken match {
      case Some(token) if token.getUserId == userId =>
        Right(ActorFlow.actorRef { out => MyWebSocketActor.props(out, channelId, token.getScreenName)})
      case _ => Left(Forbidden)
    })
  }

  object MyWebSocketActor {
    def props(out: ActorRef, channelId: String, userName: String) = Props(new MyWebSocketActor(out, channelId, userName))
  }

  class MyWebSocketActor(out: ActorRef, channelId: String, userName: String) extends Actor {


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
      case msg: String =>
        myRoom.actorSet.foreach { out =>
          out ! ("I received your message: " + msg)
        }
    }

    override def preStart(): Unit = {
      myRoom.actorSet = myRoom.actorSet + out
      myRoom.userNameSet = myRoom.userNameSet + userName
      println(myRoom.userNameSet)

      myRoom.actorSet.foreach { out =>
        out ! myRoom.userNameSet.mkString("\n")
      }


    }

    override def postStop(): Unit = {
      myRoom.actorSet = myRoom.actorSet - out
      myRoom.userNameSet = myRoom.userNameSet - userName

      myRoom.actorSet.foreach { out =>
        out ! myRoom.userNameSet.mkString("\n")
      }
    }

  }

}
