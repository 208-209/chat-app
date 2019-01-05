package object models {

  import scalikejdbc._

  def channelFindOne(channelId: String): Option[Channel] = DB readOnly { implicit session =>
    sql"""
       select *
       from channels
       where channelId = ${channelId}
    """.map { rs =>
      Channel(
        rs.string("channelId"),
        rs.string("channelName"),
        rs.string("description"),
        rs.longOpt("createdBy"),
        rs.offsetDateTime("updatedAt")
      )
    }.single().apply()
  }

  def channelUpsert(channel: Channel): Unit = DB localTx { implicit session =>
    sql"""
       insert into channels (channelId, channelName, description, createdBy, updatedAt)
       values (${channel.channelId}, ${channel.channelName}, ${channel.description}, ${channel.createdBy}, ${channel.updatedAt})
       on conflict (channelId)
       do update set channelName = ${channel.channelName}, description = ${channel.description}, updatedAt = ${channel.updatedAt}
    """.update().apply()
  }

  def channelAndMessageDelete(channelId: String): Unit = DB localTx { implicit session =>
    sql"""
       delete from messages
       where channelId = $channelId
    """.update().apply()

    sql"""
       delete from channels
       where channelId = $channelId
    """.update().apply()
  }


}
