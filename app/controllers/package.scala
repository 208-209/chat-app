import scalikejdbc._

import java.time.ZoneId
import java.time.format.DateTimeFormatter

import models._

package object controllers {

  val ADMIN_TWITTER_ID = "945597672450166784".toLong
  val messageFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss").withZone(ZoneId.of("Asia/Tokyo"))
  val channelFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日").withZone(ZoneId.of("Asia/Tokyo"))

  /**
    * そのユーザーIDの者が管理人であるか
    * @param userId ユーザーID
    * @return 真偽値
    */
  def isAdmin(userId: Long): Boolean = userId == ADMIN_TWITTER_ID


  /**
    * このユーザーIDがチャンネルにアクセスできるメンバーの一員であるか
    * @param userId ユーザーID
    * @param channel チャンネル
    * @return 真偽値
    */
  def isMember(userId: Long, channel: Channel): Boolean = {
    channel.members.split(",").map(_.toLong).contains(userId)
  }

  /**
    * このユーザーIDがチャンネルチャンネル作成者のものと一致するか
    * また、generalチャンネルの編集と削除はできない
    * @param usrId ユーザーID
    * @param channel 編集・削除するチャンネル
    * @return 真偽値
    */
  def isMineChannel(usrId: Long, channel: Channel): Boolean = {
    channel.createdBy == usrId && channel.channelId != "general"
  }

  def userUpsert(user: User): Unit = DB localTx { implicit session =>
    sql"""
       insert into users (userId, userName, profileImageUrl)
       values (${user.userId}, ${user.userName}, ${user.profileImageUrl})
       on conflict (userId)
       do update set userName = ${user.userName}, profileImageUrl = ${user.profileImageUrl}
    """.update().apply()
  }

  def userFindById(userId: Long): Option[User] = DB readOnly { implicit session =>
    sql"""
       select *
       from users
       where userId = $userId
    """.map { rs =>
      User(rs.long("userId"), rs.string("userName"), rs.string("profileImageUrl"))
    }.single().apply()
  }

  def userFindAll(): Seq[User] = DB readOnly { implicit session =>
    sql"""
       select *
       from users
       order by userName asc
    """.map { rs =>
      User(rs.long("userId"), rs.string("userName"), rs.string("profileImageUrl"))
    }.list().apply()
  }


  def channelInsert(channel: Channel): Unit = DB localTx { implicit session =>
    sql"""
       insert into channels (channelId, channelName, purpose, isPublic, members, createdBy, updatedAt)
       values (${channel.channelId}, ${channel.channelName}, ${channel.purpose}, ${channel.isPublic}, ${channel.members}, ${channel.createdBy}, ${channel.updatedAt})
    """.update().apply()
  }

  def channelUpdate(channel: Channel): Unit = DB localTx { implicit session =>
    sql"""
       update channels
       set channelName = ${channel.channelName}, purpose = ${channel.purpose}, isPublic = ${channel.isPublic}, members = ${channel.members}, updatedAt = ${channel.updatedAt}
       where channelId = ${channel.channelId}
    """.update().apply()
  }

  def channelFindById(channelId: String): Option[Channel] = DB readOnly { implicit session =>
    sql"""
       select *
       from channels
       where channelId = ${channelId}
    """.map { rs =>
      Channel(
        rs.string("channelId"),
        rs.string("channelName"),
        rs.string("purpose"),
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

  def channelFindAll(): Seq[Channel] = DB readOnly { implicit session =>
    sql"""
       select *
       from channels
       order by updatedAt desc
    """.map { rs =>
      Channel(
        rs.string("channelId"),
        rs.string("channelName"),
        rs.string("purpose"),
        rs.boolean("isPublic"),
        rs.string("members"),
        rs.long("createdBy"),
        rs.offsetDateTime("updatedAt")
      )
    }.list().apply()
  }

  def deleteChannelAggregate(channelId: String): Unit = DB localTx { implicit session =>
    sql"""
       delete from messages
       where channelId = $channelId
    """.update().apply()

    sql"""
       delete from bookmarks
       where channelId = $channelId
    """.update().apply()

    sql"""
       delete from channels
       where channelId = $channelId
    """.update().apply()
  }


  def createBookmarkMap(userId: Long): Map[String, Boolean] = DB readOnly { implicit session =>
    sql"""
       SELECT *
       FROM bookmarks
       WHERE userId = $userId
    """.map { rs => rs.string("channelId") -> rs.boolean("isBookmark")
    }.list().apply().toMap
  }

  def bookmarkUpsert(bookmark: Bookmark): Unit = DB localTx { implicit session =>
    sql"""
       INSERT INTO bookmarks (channelId, userId, isBookmark)
       VALUES (${bookmark.channelId}, ${bookmark.userId}, ${bookmark.isBookmark})
       ON CONFLICT (channelId, userId)
       DO UPDATE SET isBookmark = ${bookmark.isBookmark}
    """.update().apply()
  }

  def bookmarkAndChannelFindAll(userId: Long): Seq[(Bookmark, Channel)] = DB readOnly { implicit  session =>
    val (b, c) = (Bookmark.syntax("b"), Channel.syntax("c"))
    sql"""
       select ${b.result.*}, ${c.result.*}
       from ${Bookmark.as(b)}
       inner join ${Channel.as(c)} on ${b.channelId} = ${c.channelId}
       where userId = $userId and isBookmark = true
       order by updatedAt desc
    """.map { implicit rs => (Bookmark(b.resultName), Channel(c.resultName))}.list().apply()
  }

  def messageInsert(msg: Message): Unit = DB localTx { implicit session =>
    sql"""
       insert into messages (messageId, message, channelId, createdBy, updatedAt)
       values (${msg.messageId}, ${msg.message}, ${msg.channelId}, ${msg.createdBy}, ${msg.updatedAt})
    """.update().apply()
  }

  def messageFindById(messageId: String): Option[Message] = DB readOnly { implicit session =>
    sql"""
       select *
       from messages
       where messageId = ${messageId}
    """.map { rs =>
      Message(
        rs.string("messageId"),
        rs.string("message"),
        rs.string("channelId"),
        rs.long("createdBy"),
        rs.offsetDateTime("updatedAt")
      )
    }.single().apply()
  }

  def messageAndUserFindAll(channelId: String): Seq[(Message, User)] = DB readOnly { implicit session =>
    val (m, u) = (Message.syntax("m"), User.syntax("u"))
    sql"""
       select ${m.result.*}, ${u.result.*}
       from ${Message.as(m)}
       inner join ${User.as(u)} on ${m.createdBy} = ${u.userId}
       where channelId = $channelId
       order by updatedAt asc
    """.map { implicit rs => (Message(m.resultName), User(u.resultName))}.list().apply()
  }

  def deleteMessage(messageId: String): Unit = DB localTx { implicit session =>
    sql"""
       delete from messages
       where messageId = $messageId
    """.update().apply()
  }

}
