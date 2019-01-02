package controllers

import javax.inject._
import play.api.cache.SyncCacheApi
import play.api.mvc._

@Singleton
class HomeController @Inject()(val cache: SyncCacheApi, cc: ControllerComponents) extends TwitterLoginController(cc) {

  def index() = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>
    Ok(views.html.index(request.accessToken))
  }
}
