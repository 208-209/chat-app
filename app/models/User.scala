package models

case class User(userId: Option[Long], userName: Option[String])

object User {

  def apply(accessToken: twitter4j.auth.AccessToken): User = {
    val userId = accessToken.getUserId
    val userName = accessToken.getScreenName
    User(Some(userId), Some(userName))
  }

}