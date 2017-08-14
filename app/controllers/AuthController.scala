package controllers

import dal.AuthorsDAO
import javax.inject.Inject

import jp.t2v.lab.play2.auth.LoginLogout
import org.webjars.play.RequireJS
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class LoginData(username:String, password:String)

class AuthController @Inject() (val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider,
                                override protected val authorsDAO: AuthorsDAO,
                                implicit val webJarAssets: WebJarAssets, implicit val requireJS: RequireJS)
extends Controller with LoginLogout with AuthConfigImpl with I18nSupport {

  val loginForm = Form(
    mapping("nickname" -> nonEmptyText, "password" -> text
    ) (LoginData.apply)(LoginData.unapply)
  )

  def login: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def logout: Action[AnyContent] = Action.async { implicit request =>
    gotoLogoutSucceeded
  }

  def authenticate: Action[AnyContent] = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.login(formWithErrors))),
      data =>
        authorsDAO.authenticate(data.username, data.password).flatMap {
          case Some(user) => gotoLoginSucceeded(user.id)
          case None => Future.successful(BadRequest(views.html.login(loginForm.fill(data).withGlobalError("user.not_authenticated"))))
        }
    )
  }

}
