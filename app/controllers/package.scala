import java.time.ZoneId
import java.time.format.DateTimeFormatter

import models._

package object controllers {

  val ADMIN_TWITTER_ID = "945597672450166784".toLong
  val messageFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss").withZone(ZoneId.of("Asia/Tokyo"))
  val channelFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日").withZone(ZoneId.of("Asia/Tokyo"))

  /**
    * ユーザーIDの者が管理人である
    * @param userId ユーザーID
    * @return 真偽値
    */
  def isAdmin(userId: Long): Boolean = {
    userId == ADMIN_TWITTER_ID
  }

  /**
    * ユーザーIDがチャンネルにアクセスできるメンバーの一員である
    * @param userId ユーザーID
    * @param channel チャンネル
    * @return 真偽値
    */
  def isMember(userId: Long, channel: Channel): Boolean = {
    channel.members.split(",").map(_.toLong).contains(userId)
  }

  /**
    * ユーザーIDがチャンネルチャンネル作成者のものと一致する
    * また、generalチャンネルの編集と削除はできない
    * @param usrId ユーザーID
    * @param channel 編集・削除するチャンネル
    * @return 真偽値
    */
  def isMineChannel(usrId: Long, channel: Channel): Boolean = {
    channel.createdBy == usrId && channel.channelId != "general"
  }

}
