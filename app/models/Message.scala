package models

import scalikejdbc._
import java.time.OffsetDateTime

case class Message(messageId: String, message: String, channelId: String, createdBy: Long, updatedAt: OffsetDateTime)

object Message extends SQLSyntaxSupport[Message] {
  override val tableName = "messages"
  override val useSnakeCaseColumnName = false
  val m = Message.syntax("m")

  def apply(m: ResultName[Message])(implicit rs: WrappedResultSet): Message = {
    new Message(
      messageId = rs.string(m.messageId),
      message = rs.string(m.message),
      channelId = rs.string(m.channelId),
      createdBy = rs.long(m.createdBy),
      updatedAt = rs.offsetDateTime(m.updatedAt)
    )
  }

}