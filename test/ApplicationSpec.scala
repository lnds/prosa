
import org.scalatestplus.play._
import play.api.{Configuration, Environment}
import play.api.db.evolutions._
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



}