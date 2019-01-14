package models

import scalikejdbc._

case class Bookmark(channelId: String, userId: Long, isBookmark: Boolean)

object Bookmark extends SQLSyntaxSupport[Bookmark] {
  override val tableName = "bookmarks"
  override val useSnakeCaseColumnName = false
  val b = Channel.syntax("b")

  def apply(b: ResultName[Bookmark])(implicit rs: WrappedResultSet): Bookmark = {
    new Bookmark(
      channelId = rs.string(b.channelId),
      userId = rs.long(b.userId),
      isBookmark = rs.boolean(b.isBookmark)
    )
  }

}
