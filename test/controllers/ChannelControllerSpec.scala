package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._

class ChannelControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "GET /general" should {

    "ログイン認証なしにチャンネルにアクセスすると、Twitter認証のページにリダイレクトされる" in {
      val controller = inject[ChannelController]
      val home = controller.read("general").apply(FakeRequest(GET, "/general"))

      status(home) mustBe 303
    }

//    "ログイン時はユーザー名が表示される" in {
//
//    }
  }
}
