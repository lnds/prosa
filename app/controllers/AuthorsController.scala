package controllers

import dal.AuthorsDAO
import javax.inject.Inject

import jp.t2v.lab.play2.auth.AuthElement
import models.Writer
import org.mindrot.jbcrypt.BCrypt
import org.webjars.play.RequireJS
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller}

import scalaz.Scalaz._

class AuthorsController @Inject() (val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider,
                                   val  authorsDAO:AuthorsDAO,
                                   implicit val webJarAssets: WebJarAssets, implicit val requireJS: RequireJS)
  extends Controller with TokenValidateElement with AuthElement with AuthConfigImpl with I18nSupport  {

  val changePasswordForm = Form(
    tuple(
      "password" -> nonEmptyText,
      "new_password" -> nonEmptyText,
      "confirm_password" -> nonEmptyText
    ).verifying(Messages("main.error.paswords_must_match"), result => result match {
      case (_,p1,p2) => p1 === p2
      }
    )
  )

  def changePassword(): Action[AnyContent] = StackAction(AuthorityKey -> Writer,IgnoreTokenValidation -> None) { implicit request =>
    Ok(views.html.change_password(changePasswordForm))
  }


  def savePassword(): Action[AnyContent] = StackAction(AuthorityKey -> Writer) { implicit request =>
    changePasswordForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.change_password(formWithErrors)),
      formOk => {
        val (password, newPassword, _) = formOk
        if (!BCrypt.checkpw(password, loggedIn.password))
          BadRequest(views.html.change_password(changePasswordForm.withError("password", "main.error.bad_current_password")))
        else {
          authorsDAO.changePassword(loggedIn, newPassword)
          Redirect(routes.BlogsGuestController.index()).flashing("success" -> "main.success.password_changed")
        }
      })

  }

}
