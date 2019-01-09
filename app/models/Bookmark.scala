package models

import scalikejdbc._

case class Bookmark(channelId: String, createdBy: Option[Long], isBookmark: Boolean)

object Bookmark extends SQLSyntaxSupport[Bookmark] {
  override val tableName = "bookmarks"
  override val useSnakeCaseColumnName = false
  val b = Channel.syntax("b")

  def apply(b: ResultName[Bookmark])(implicit rs: WrappedResultSet): Bookmark = {
    new Bookmark(
      channelId = rs.string(b.channelId),
      createdBy = rs.longOpt(b.createdBy),
      isBookmark = rs.boolean(b.isBookmark)
    )
  }

}
