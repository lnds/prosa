package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.LoginLogout
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import services.AuthorService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class LoginData(username:String, password:String)

class AuthController @Inject() (val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider) extends Controller with LoginLogout with AuthConfigImpl with I18nSupport {


  val loginForm = Form(
    mapping("nickname" -> nonEmptyText, "password" -> text
    ) (LoginData.apply)(LoginData.unapply)
  )

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded
  }

  def authenticate = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.login(formWithErrors))),
      data =>
        AuthorService.authenticate(data.username, data.password).flatMap {
          case Some(user) => gotoLoginSucceeded(user.id)
          case None => Future.successful(BadRequest(views.html.login(loginForm.fill(data).withGlobalError("user.not_authenticated"))))
        }
    )
  }

}
