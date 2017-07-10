
import org.scalatestplus.play._
import play.api.{Configuration, Environment}
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends PlaySpec with OneAppPerTest  {


  val messagesApi = new DefaultMessagesApi(Environment.simple(), Configuration.reference, new DefaultLangs(Configuration.reference))


  "Routes" should {

    "send 404 on a bad request" in  {

      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

  }

  "HomeController" should {

    "render the index page" in {

      val request = FakeRequest(GET, "/")
      val _ = route(app, request).get


      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")

      val expected = messagesApi.preferred(request).messages("prosa.main.content.index")
      contentAsString(home) must include (expected)
    }

  }


}