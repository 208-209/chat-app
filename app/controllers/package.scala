import java.time.ZoneId
import java.time.format.DateTimeFormatter

import models._

package object controllers {

  val ADMIN_TWITTER_ID = "945597672450166784".toLong
  val messageFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss").withZone(ZoneId.of("Asia/Tokyo"))
  val channelFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日").withZone(ZoneId.of("Asia/Tokyo"))

  /**
    * そのユーザーIDの者が管理人であるか
    * @param userId
    * @return
    */
  def isAdmin(userId: Long): Boolean = userId == ADMIN_TWITTER_ID


  /**
    * このユーザーIDがチャンネルにアクセスできるメンバーの一員であるか
    * @param userId
    * @param channel
    * @return
    */
  def isMember(userId: Long, channel: Channel): Boolean = {
    channel.members.split(",").map(_.toLong).contains(userId)
  }

  /**
    * このユーザーIDがチャンネルチャンネル作成者のものと一致するか
    * また、generalチャンネルの編集と削除はできない
    * @param usrId
    * @param channel
    * @return
    */
  def isMineChannel(usrId: Long, channel: Channel): Boolean = {
    channel.createdBy == usrId && channel.channelId != "general"
  }



}
