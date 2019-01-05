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

  def findOne(channelId: String): Option[(Channel, User)] = DB readOnly { implicit session =>
    val (c, u) = (Channel.syntax("c"), User.syntax("u"))
    sql"""
       select ${c.result.*}, ${u.result.*}
       from ${Channel.as(c)}
       inner join ${User.as(u)} on ${c.createdBy} = ${u.userId}
       where channelId = ${channelId}
    """.map { implicit rs =>
      (Channel(c.resultName), User(u.resultName))
    }.single().apply()
  }

  def upsert(channel: Channel): Unit = DB localTx { implicit session =>
    sql"""
       insert into channels (channelId, channelName, description, createdBy, updatedAt)
       values (${channel.channelId}, ${channel.channelName}, ${channel.description}, ${channel.createdBy}, ${channel.updatedAt})
       on conflict (channelId)
       do update set channelName = ${channel.channelName}, description = ${channel.description}, updatedAt = ${channel.updatedAt}
    """.update().apply()
  }




}
