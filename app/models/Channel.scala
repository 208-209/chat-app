package models

import java.time.OffsetDateTime

case class Channel(channelId: String, channelName: String, description: String, open: Boolean, createdBy: Option[Long], updatedAt: OffsetDateTime)

object Channel {

}