package controllers

import javax.inject._
import play.api.cache.SyncCacheApi
import play.api.i18n.I18nSupport
import play.api.mvc._
import models._
import play.libs.Json

@Singleton
class BookmarkController @Inject()(val cache: SyncCacheApi, cc: ControllerComponents) extends TwitterLoginController(cc) with I18nSupport {

  def ajax(channelId: String, userId: Long) = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>

    println(request.body)

    val isBookmark = !request.body.asFormUrlEncoded.get("bookmark").head.toBoolean

    val bookmark = Bookmark(channelId, Some(userId), isBookmark)

    bookmarkUpsert(bookmark)

    val result = s"""{"bookmark": "${isBookmark}"}"""

    Ok(result)
  }
}
