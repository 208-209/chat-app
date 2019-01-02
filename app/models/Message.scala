package models

import java.time.OffsetDateTime

case class Message(messageId: Long, message: String, channelId: String, createdBy: Option[Long], updatedAt: OffsetDateTime)

object Message {

}