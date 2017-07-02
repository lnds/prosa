package controllers

import java.security.SecureRandom
import jp.t2v.lab.play2.stackc.{RequestAttributeKey, RequestWithAttributes, StackableController}
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import scala.concurrent.Future
import scala.util.Random

trait TokenValidateElement extends StackableController {

  self: Controller =>

  private val preventingCsrfTokenSessionKey = "preventingCsrfToken"

  private val tokenForm = Form(PreventingCsrfToken.formKey -> text)

  private val random = new Random(new SecureRandom)
  private[this] val table = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') ++ "^`~:/?,.{[}}|+_()*^%$#@!"

  private def generateToken: PreventingCsrfToken = PreventingCsrfToken {
    Stream.continually(random.nextInt(table.size)).map(table).take(32).mkString
  }

  case object PreventingCsrfTokenKey extends RequestAttributeKey[PreventingCsrfToken]
  case object IgnoreTokenValidation extends RequestAttributeKey[Unit]

  private def validateToken(request: Request[_]): Boolean = (for {
    tokenInForm    <- tokenForm.bindFromRequest()(request).value
    tokenInSession <- request.session.get(preventingCsrfTokenSessionKey)
  } yield tokenInForm == tokenInSession) getOrElse false

  override def proceed[A](request: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Future[Result]): Future[Result] = {
    if (isIgnoreTokenValidation(request) || validateToken(request)) {
      implicit val ctx = StackActionExecutionContext(request)
      val newToken = generateToken
      super.proceed(request.set(PreventingCsrfTokenKey, newToken))(f) map {
        _.withSession(preventingCsrfTokenSessionKey -> newToken.value)
      }
    } else {
      Future.successful(BadRequest("Invalid preventing CSRF token"))
    }
  }

  implicit def isIgnoreTokenValidation(implicit request: RequestWithAttributes[_]): Boolean =
    request.get(IgnoreTokenValidation).isDefined

  implicit def preventingCsrfToken(implicit request: RequestWithAttributes[_]): PreventingCsrfToken =
    request.get(PreventingCsrfTokenKey).orNull

}
case class PreventingCsrfToken(value: String)

object PreventingCsrfToken {

  val formKey = "preventingCsrfToken"

}