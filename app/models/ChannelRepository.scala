package models

import scalikejdbc._

object ChannelRepository {

  def insert(channel: Channel): Unit = DB localTx { implicit session =>
    sql"""
       insert into channels (channelId, channelName, description, createdBy, updatedAt)
       values (${channel.channelId}, ${channel.channelName}, ${channel.description}, ${channel.createdBy}, ${channel.updatedAt})
    """.update().apply()
  }

  def findAll(): Seq[Channel] = DB readOnly { implicit session =>
    sql"""
       select *
       from channels
       order by updatedAt asc
    """.map { rs =>
      Channel(
        rs.string("channelId"),
        rs.string("channelName"),
        rs.string("description"),
        rs.longOpt("createdBy"),
        rs.offsetDateTime("updatedAt")
      )
    }.list().apply()
  }






}
