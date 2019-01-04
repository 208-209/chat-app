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

@Singleton
class MessageController @Inject() (val cache: SyncCacheApi, cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends TwitterLoginController(cc) {

  def socket(channelId: String, userId: Long) = WebSocket.acceptOrResult[String, String] { request =>

    val sessionIdOpt = request.cookies.get(sessionIdName).map(_.value)
    val accessToken = sessionIdOpt.flatMap(cache.get[AccessToken])

    println(accessToken)

    Future.successful(accessToken match {
      case Some(token) if token.getUserId == userId =>
        Right(ActorFlow.actorRef { out => MyWebSocketActor.props(out)})
      case _ => Left(Forbidden)
    })
  }

  object MyWebSocketActor {
    def props(out: ActorRef) = Props(new MyWebSocketActor(out))
  }

  class MyWebSocketActor(out: ActorRef) extends Actor {
    def receive = {
      case msg: String =>
        out ! ("I received your message: " + msg)
    }
  }

}
