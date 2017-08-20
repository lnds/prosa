
import org.scalatestplus.play._
import play.api.{Configuration, Environment}
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test._
import play.api.test.Helpers._
import buildinfo.BuildInfo

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

      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")

      var expected = messagesApi.preferred(request).messages("prosa.main.content.index")
      contentAsString(home) must include (expected)

      expected = messagesApi.preferred(request).messages("menu.login")
      contentAsString(home) must include (expected)

      expected = messagesApi.preferred(request).messages("footer.prosa", buildinfo.BuildInfo.version)
      contentAsString(home) must include (expected)

    }

  }

  "BlogsGuestController" should {
    "have a login option on menu" in {
      val request = FakeRequest(GET, "/blogs")

      val blogs = route(app, request).get

      status(blogs) mustBe OK
      contentType(blogs) mustBe Some("text/html")
      val expected = messagesApi.preferred(request).messages("menu.login")
      contentAsString(blogs) must include (expected)

    }
  }

  "AuthController" should {

    "login admin" in {
      val request = FakeRequest(GET, "/login")
      val login = route(app, request).get

      status(login) mustBe OK
      contentType(login) mustBe Some("text/html")
      contentAsString(login) must include("")

      val postRequest = FakeRequest(POST, "/authenticate").withFormUrlEncodedBody(
        "nickname" ->"admin",
        "password" -> "admin"
      )
      val authenticate = route(app, postRequest).get
      status(authenticate) mustBe SEE_OTHER
      val uri = session(authenticate).get("redirect_uri")
      uri.nonEmpty && (uri.getOrElse("") == "/blogs")

    }
  }



}