package models

import scalikejdbc._

case class User(userId: Option[Long], userName: Option[String])

object User extends SQLSyntaxSupport[User] {
  override val tableName = "users"
  override val useSnakeCaseColumnName = false
  val u = User.syntax("u")

  def apply(u: ResultName[User])(implicit rs: WrappedResultSet): User = {
    new User(userId = rs.longOpt(u.userId), userName = rs.stringOpt(u.userName))
  }

  def apply(accessToken: twitter4j.auth.AccessToken): User = {
    val userId = accessToken.getUserId
    val userName = accessToken.getScreenName
    User(Some(userId), Some(userName))
  }

}