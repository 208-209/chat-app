package models

import scalikejdbc._

object UserRepository {

  def upsert(user: User): Unit = DB localTx { implicit session =>
    sql"""
         |insert into users (userId, userName)
         |values (${user.userId}, ${user.userName})
         |on conflict (userId)
         |do update set userName = ${user.userName}
    """.stripMargin.update().apply()
  }

}
