package object models {

  import scalikejdbc._

  def channelFindById(channelId: String): Option[Channel] = DB readOnly { implicit session =>
    sql"""
       select *
       from channels
       where channelId = ${channelId}
    """.map { rs =>
      Channel(
        rs.string("channelId"),
        rs.string("channelName"),
        rs.string("description"),
        rs.boolean("isPublic"),
        rs.string("members"),
        rs.long("createdBy"),
        rs.offsetDateTime("updatedAt")
      )
    }.single().apply()
  }

  def channelAndUserFindOne(channelId: String): Option[(Channel, User)] = DB readOnly { implicit session =>
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

  def channelUpsert(channel: Channel): Unit = DB localTx { implicit session =>
    sql"""
       insert into channels (channelId, channelName, description, isPublic, members, createdBy, updatedAt)
       values (${channel.channelId}, ${channel.channelName}, ${channel.description}, ${channel.isPublic}, ${channel.members}, ${channel.createdBy}, ${channel.updatedAt})
       on conflict (channelId)
       do update set channelName = ${channel.channelName}, description = ${channel.description}, isPublic = ${channel.isPublic}, members = ${channel.members}, updatedAt = ${channel.updatedAt}
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

  def channelFindAll(): Seq[Channel] = DB readOnly { implicit session =>
    sql"""
       select *
       from channels
       order by updatedAt asc
    """.map { rs =>
      Channel(
        rs.string("channelId"),
        rs.string("channelName"),
        rs.string("description"),
        rs.boolean("isPublic"),
        rs.string("members"),
        rs.long("createdBy"),
        rs.offsetDateTime("updatedAt")
      )
    }.list().apply()
  }

  def channelInsert(channel: Channel): Unit = DB localTx { implicit session =>
    sql"""
       insert into channels (channelId, channelName, description, isPublic, members, createdBy, updatedAt)
       values (${channel.channelId}, ${channel.channelName}, ${channel.description}, ${channel.isPublic}, ${channel.members}, ${channel.createdBy}, ${channel.updatedAt})
    """.update().apply()
  }



  def bookmarkAndChannelFindAll(userId: Long): Seq[(Bookmark, Channel)] = DB readOnly { implicit  session =>
    val (b, c) = (Bookmark.syntax("b"), Channel.syntax("c"))
    sql"""
       select ${b.result.*}, ${c.result.*}
       from ${Bookmark.as(b)}
       inner join ${Channel.as(c)} on ${b.channelId} = ${c.channelId}
       where userId = $userId and isBookmark = true
       order by updatedAt
    """.map { implicit rs => (Bookmark(b.resultName), Channel(c.resultName))}.list().apply()
  }


  def bookmarkTuple(userId: Long): Seq[(String, Boolean)] = DB readOnly { implicit session =>
    sql"""
          SELECT *
          FROM bookmarks
          WHERE userId = $userId
       """.map { rs => (rs.string("channelId") -> rs.boolean("isBookmark"))
    }.list().apply()

  }

  def bookmarkUpsert(bookmark: Bookmark): Unit = DB localTx { implicit session =>
    sql"""
       INSERT INTO bookmarks (channelId, userId, isBookmark)
       VALUES (${bookmark.channelId}, ${bookmark.userId}, ${bookmark.isBookmark})
       ON CONFLICT (channelId, userId)
       DO UPDATE SET isBookmark = ${bookmark.isBookmark}
    """.update().apply()
  }

  def userFindAll(): Seq[User] = DB readOnly { implicit session =>
    sql"""
       select *
       from users
       order by userName
    """.map { rs =>
      User(rs.long("userId"), rs.string("userName"))
    }.list().apply()
  }

  def userTuple(): Seq[(Option[Long], Option[String])] = DB readOnly { implicit session =>
    sql"""
          SELECT *
          FROM users
       """.map { rs => (rs.longOpt("userId") -> rs.stringOpt("userName"))
    }.list().apply()

  }

}
