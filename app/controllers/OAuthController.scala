package controllers

import javax.inject.Inject

import infrastructure.twitter.{TwitterAuthenticator, TwitterException}
import play.api.{Configuration}
import play.api.cache.SyncCacheApi
import play.api.mvc.ControllerComponents

import scala.concurrent.duration._

import models._

class OAuthController @Inject()(
                                 cc: ControllerComponents,
                                 twitterAuthenticator: TwitterAuthenticator,
                                 configuration: Configuration,
                                 val cache: SyncCacheApi
                               ) extends TwitterLoginController(cc) {

  val documentRootUrl = configuration.get[String]("ca.documentrooturl")

  def login = TwitterLoginAction { request =>
    try {
      val callbackUrl = documentRootUrl + routes.OAuthController.oauthCallback(None).url
      val authenticationUrl = twitterAuthenticator.startAuthentication(request.sessionId, callbackUrl)
      Redirect(authenticationUrl)
    } catch {
      case e: TwitterException => BadRequest(e.message)
    }
  }

  def oauthCallback(verifierOpt: Option[String]) = TwitterLoginAction { request =>
    try {
      verifierOpt.map(twitterAuthenticator.getAccessToken(request.sessionId, _)) match {
        case Some(accessToken) =>
          cache.set(request.sessionId, accessToken._1, 30.minutes)
          // User情報をデータベースに登録
          userUpsert(User(accessToken))

          // オープンリダイレクタ脆弱性対策でUUIDであるか判定
          val from = cache.get[String]("loginFrom") match {
            case Some(loginFrom) if loginFrom.matches("""[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}""") =>
              cache.remove("loginFrom")
              loginFrom
            case _ => "general"
          }
          Redirect(documentRootUrl + routes.ChannelController.read(from).url)
        case None => BadRequest(s"Could not get OAuth verifier. SessionId: ${request.sessionId}")
      }
    } catch {
      case e: TwitterException => BadRequest(e.message)
    }
  }

  def logout = TwitterLoginAction { request =>
    cache.remove(request.sessionId)
    Redirect(documentRootUrl + routes.HomeController.index().url)
  }
}