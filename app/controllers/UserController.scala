package controllers

import javax.inject._
import play.api.cache.SyncCacheApi
import play.api.i18n.I18nSupport
import play.api.mvc._

import models._

@Singleton
class UserController @Inject()(val cache: SyncCacheApi, cc: ControllerComponents) extends TwitterLoginController(cc) with I18nSupport {

  def delete(userId: Long) = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>
    request.accessToken match {
      case Some(token) =>
        userFindById(userId) match {
          case Some(user) if user.userId == token.getUserId =>
            deleteUser(user)
            Redirect(routes.OAuthController.logout())
          case _ => NotFound("指定されたユーザーは存在しない、または、アカウントを解除する権限がありません")
        }
      case None =>Redirect(routes.OAuthController.login())
    }
  }
}
