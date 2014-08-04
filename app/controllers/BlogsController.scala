package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.{Editor, Blogs}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.Controller
import play.api.db.slick.DB
import play.api.Play.current

object BlogsController extends Controller with DBElement with TokenValidateElement with AuthElement with AuthConfigImpl {

  case class BlogData(name:String,alias:String,description:String,image:Option[String],logo:Option[String],url:Option[String])

  val createForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "alias" -> nonEmptyText.verifying(Messages("blogs.error.alias_format"), checkAliasName _).verifying(Messages("blogs.error.duplicate_alias"), checkAliasNotExists _),
      "description" -> nonEmptyText,
      "image" -> optional(text),
      "logo" -> optional(text),
      "url" -> optional(text)
    )(BlogData.apply)(BlogData.unapply)
  )

  def checkAliasName(alias:String) = alias.matches("[a-zA-Z0-9_-]+")   && alias.length() <= 32

  def checkAliasNotExists(alias:String) = {
    DB.withSession { implicit session =>
      val blog = Blogs.findByAlias(alias)
      blog.isEmpty
    }
  }

  def create = StackAction(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    Ok(views.html.blogs_new(createForm, loggedIn))
  }

  def save = StackAction(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    createForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.blogs_new(formWithErrors, loggedIn)),
      blogData => {
        Blogs.create(loggedIn, blogData.name, blogData.alias, blogData.description, blogData.image, blogData.logo, blogData.url)
        Redirect(routes.BlogsGuestController.index()).flashing("success" -> Messages("blogs.success.created"))
      }
    )
  }

}
