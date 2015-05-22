package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.Writer
import org.mindrot.jbcrypt.BCrypt
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.Controller
import services.AuthorService

object AuthorsController extends Controller with DBElement  with TokenValidateElement with AuthElement with AuthConfigImpl {

  val changePasswordForm = Form(
    tuple(
      "password" -> nonEmptyText,
      "new_password" -> nonEmptyText,
      "confirm_password" -> nonEmptyText
    ).verifying(Messages("main.error.paswords_must_match"), result => result match {
      case (_,p1,p2) => p1 == p2
      }
    )
  )

  def changePassword = StackAction(AuthorityKey -> Writer,IgnoreTokenValidation -> None) { implicit request =>
    Ok(views.html.change_password(changePasswordForm))
  }


  def savePassword = StackAction(AuthorityKey -> Writer) { implicit request =>
    changePasswordForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.change_password(formWithErrors)),
      formOk => {
        val (password, new_password, _) = formOk
        if (!BCrypt.checkpw(password, loggedIn.password))
          BadRequest(views.html.change_password(changePasswordForm.withError("password", "main.error.bad_current_password")))
        else {
          AuthorService.changePassword(loggedIn, new_password)
          Redirect(routes.BlogsGuestController.index()).flashing("success" -> "main.success.password_changed")
        }
      })

  }

}
