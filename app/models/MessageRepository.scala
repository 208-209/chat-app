package models

import scalikejdbc._

object MessageRepository {

  def insert(msg: Message): Unit = DB localTx { implicit session =>
    sql"""
       insert into messages (message, channelId, createdBy, updatedAt)
       values (${msg.message}, ${msg.channelId}, ${msg.createdBy}, ${msg.updatedAt})
    """.update().apply()
  }

  def findAll(channelId: String): Seq[(Message, User)] = DB readOnly { implicit session =>
    val (m, u) = (Message.syntax("m"), User.syntax("u"))
    sql"""
       select ${m.result.*}, ${u.result.*}
       from ${Message.as(m)}
       inner join ${User.as(u)} on ${m.createdBy} = ${u.userId}
       where channelId = $channelId
       order by updatedAt ASC
    """.map { implicit rs => (Message(m.resultName), User(u.resultName))}.list().apply()
  }

}
