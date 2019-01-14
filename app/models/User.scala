package models

import scalikejdbc._

case class User(userId: Long, userName: String)

object User extends SQLSyntaxSupport[User] {
  override val tableName = "users"
  override val useSnakeCaseColumnName = false
  val u = User.syntax("u")

  def apply(u: ResultName[User])(implicit rs: WrappedResultSet): User = {
    new User(userId = rs.long(u.userId), userName = rs.string(u.userName))
  }

  def apply(accessToken: twitter4j.auth.AccessToken): User = {
    val userId = accessToken.getUserId
    val userName = accessToken.getScreenName
    User(userId, userName)
  }

}