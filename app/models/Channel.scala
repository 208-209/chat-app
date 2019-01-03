package models

import java.time.OffsetDateTime
import scalikejdbc._

case class Channel(channelId: String, channelName: String, description: String, createdBy: Option[Long], updatedAt: OffsetDateTime)

object Channel extends SQLSyntaxSupport[Channel] {
  override val tableName = "channels"
  override val useSnakeCaseColumnName = false
  val c = Channel.syntax("c")

  def apply(c: ResultName[Channel])(implicit rs: WrappedResultSet): Channel = {
    new Channel(
      channelId = rs.string(c.channelId),
      channelName = rs.string(c.channelName),
      description = rs.string(c.description),
      createdBy = rs.longOpt(c.createdBy),
      updatedAt = rs.offsetDateTime(c.updatedAt)
    )
  }

}