package models

import scalikejdbc._

object ChannelRepository {

  def insert(channel: Channel): Unit = DB localTx { implicit session =>
    sql"""
       insert into channels (channelId, channelName, description, createdBy, updatedAt)
       values (${channel.channelId}, ${channel.channelName}, ${channel.description}, ${channel.createdBy}, ${channel.updatedAt})
    """.update().apply()
  }


}
