package controllers

import javax.inject._
import play.api.cache.SyncCacheApi
import play.api.i18n.I18nSupport
import play.api.mvc._
import models._

@Singleton
class BookmarkController @Inject()(val cache: SyncCacheApi, cc: ControllerComponents) extends TwitterLoginController(cc) with I18nSupport {

  def ajax(channelId: String, userId: Long) = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>

    request.accessToken match {
      case Some(accessToken) =>
        channelAndUserFindOne(channelId) match {
          case Some(channel) if userId == accessToken.getUserId =>
            val isBookmark = !request.body.asFormUrlEncoded.get("bookmark").head.toBoolean
            bookmarkUpsert(Bookmark(channelId, Some(userId), isBookmark))
            val result = s"""{"bookmark": "$isBookmark"}"""
            Ok(result)
          case _ => NotFound("指定されたチャンネルは見つかりません。")
        }
      case None => Redirect(routes.OAuthController.login())
    }

  }
}
