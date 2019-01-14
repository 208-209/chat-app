package models

import scalikejdbc._

object MessageRepository {

  def insert(msg: Message): Unit = DB localTx { implicit session =>
    sql"""
       insert into messages (messageId, message, channelId, createdBy, updatedAt)
       values (${msg.messageId}, ${msg.message}, ${msg.channelId}, ${msg.createdBy}, ${msg.updatedAt})
    """.update().apply()
  }


}
