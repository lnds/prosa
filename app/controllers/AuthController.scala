package controllers

import jp.t2v.lab.play2.auth.LoginLogout
import models.Author
import org.mindrot.jbcrypt.BCrypt
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}
import services.AuthorService

import scala.concurrent.Future


object AuthController extends Controller with LoginLogout with AuthConfigImpl {
  import scala.concurrent.ExecutionContext.Implicits.global

  val loginForm = Form {
    mapping("nickname" -> nonEmptyText, "password" -> text)(authenticateAuthor)(_.map(u => (u.nickname, "")))
      .verifying("Invalid email or password", result => result.isDefined)
  }

  def authenticateAuthor(nickname:String, password:String) : Option[Author] = {
    play.api.db.slick.DB.withSession { implicit session =>
      AuthorService.findByNickname(nickname).map { author =>
        if (BCrypt.checkpw(password, author.password))
          Some(author)
        else
          None
      }.getOrElse(None)
    }
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded
  }

  def authenticate = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.login(formWithErrors))),
      author => gotoLoginSucceeded(author.get.id)
    )
  }

}
