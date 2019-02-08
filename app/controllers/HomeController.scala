package controllers

import javax.inject._
import play.api.cache.SyncCacheApi
import play.api.i18n.I18nSupport
import play.api.mvc._

import models._

@Singleton
class HomeController @Inject()(val cache: SyncCacheApi, cc: ControllerComponents) extends TwitterLoginController(cc) with I18nSupport {

  def index() = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>
    request.accessToken match {
      case Some(token) =>
        val user = userFindById(token.getUserId)
        Ok(views.html.index(user))
      case None =>
        Ok(views.html.index(None))
    }
  }
}
