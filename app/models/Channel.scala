package models

import scalikejdbc._
import java.time.OffsetDateTime

case class Channel(channelId: String, channelName: String, purpose: String, isPublic: Boolean, members: String, createdBy: Long, updatedAt: OffsetDateTime)

object Channel extends SQLSyntaxSupport[Channel] {
  override val tableName = "channels"
  override val useSnakeCaseColumnName = false
  val c = Channel.syntax("c")

  def apply(c: ResultName[Channel])(implicit rs: WrappedResultSet): Channel = {
    new Channel(
      channelId = rs.string(c.channelId),
      channelName = rs.string(c.channelName),
      purpose = rs.string(c.purpose),
      isPublic = rs.boolean(c.isPublic),
      members = rs.string(c.members),
      createdBy = rs.long(c.createdBy),
      updatedAt = rs.offsetDateTime(c.updatedAt)
    )
  }

}