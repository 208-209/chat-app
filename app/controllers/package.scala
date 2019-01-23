import models._

package object controllers {

  val ADMIN_TWITTER_ID = "937000074978107392".toLong

  /**
    * チャンネルページの観覧をユーザーIDで絞り込む
    * パブリックチャンネル => 全員可
    * プライベートチャンネル => チャンネルのメンバーに含まれるかどうか
    *
    * @param token
    * @param channel
    * @return
    */
  def isReadable(token: twitter4j.auth.AccessToken, channel: (Channel, User)): Boolean = {
    channel._1.isPublic || channel._1.members.split(",").map(_.toLong).contains(token.getUserId)
  }



}
