package models

import scalikejdbc._

case class User(userId: Long, userName: String, profileImageUrl: String, deleted: Boolean)

object User extends SQLSyntaxSupport[User] {
  override val tableName = "users"
  override val useSnakeCaseColumnName = false
  val u = User.syntax("u")

  def apply(u: ResultName[User])(implicit rs: WrappedResultSet): User = {
    new User(
      userId = rs.long(u.userId),
      userName = rs.string(u.userName),
      profileImageUrl = rs.string(u.profileImageUrl),
      deleted = rs.boolean(u.deleted)
    )
  }

  def apply(accessToken: (twitter4j.auth.AccessToken, String)): User = {
    User(accessToken._1.getUserId, accessToken._1.getScreenName, accessToken._2, false)
  }

}