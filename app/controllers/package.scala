import java.time.ZoneId
import java.time.format.DateTimeFormatter

import models.Channel

package object controllers {

  val ADMIN_TWITTER_ID = "945597672450166784".toLong
  val messageFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss").withZone(ZoneId.of("Asia/Tokyo"))
  val channelFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日").withZone(ZoneId.of("Asia/Tokyo"))

  /**
    * そのユーザーIDの者が管理人であるか
    *
    * @param userId
    * @return
    */
  def isAdmin(userId: Long): Boolean = userId == ADMIN_TWITTER_ID

  /**
    * このuserIdがプライベートチャンネルにアクセスできるメンバーの一員であるか
    *
    * @param channel
    * @param user
    * @return
    */
  def isMember(channel: Channel, userId: Long): Boolean = {
    channel.members.split(",").map(_.toLong).contains(userId)
  }




}
