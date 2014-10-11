package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.{Authors, Editor, Blogs}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.Controller
import play.api.db.slick.DB
import play.api.Play.current
import tools.PostAux

object BlogsController extends Controller with DBElement with TokenValidateElement with AuthElement with AuthConfigImpl {

  case class BlogData(id:Option[String], name:String,alias:String,description:String,image:Option[String],logo:Option[String],url:Option[String], disqus:Option[String], googleAnalytics:Option[String], useAvatarAsLogo:Option[Boolean], status:Int)

  val blogForm = Form(
    mapping(
      "id" -> optional(text),
      "name" -> nonEmptyText,
      "alias" -> nonEmptyText.verifying(Messages("blogs.error.alias_format"), checkAliasName _),
      "description" -> nonEmptyText,
      "image" -> optional(text),
      "logo" -> optional(text),
      "url" -> optional(text),
      "disqus" -> optional(text),
      "google_analytics" -> optional(text),
      "use_avatar_as_logo" -> optional(boolean),
      "status" -> number(min = Blogs.BLOG_STATUS_INACTIVE, max= Blogs.BLOG_STATUS_PUBLISHED)
    )
    (BlogData.apply)(BlogData.unapply)
    .verifying(Messages("blogs.error.duplicate_alias"), result => result match {
      case blogData => checkAliasNotExists(blogData.id, blogData.alias)
    }
    )
  )



  def checkAliasName(alias:String) = alias.matches("[a-zA-Z0-9_-]+")   && alias.length() <= 32

  /// this is ugly
  def checkAliasNotExists(idOpt:Option[String], alias:String) = {
    DB.withSession { implicit session =>
      idOpt match {
        case None =>
          Blogs.findByAlias(alias).isEmpty
        case Some(id) =>
          Blogs.findById(id).map { blog =>
            if (blog.alias == alias)
              true
            else
              Blogs.findByAlias(alias).isEmpty
          }.getOrElse(Blogs.findByAlias(alias).isEmpty)
      }
    }
  }

  def create = StackAction(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    val ownerEmail = Authors.findById(loggedIn.id).map { _.email }.orNull
    Ok(views.html.blogs_form(None, blogForm, loggedIn, PostAux.avatarUrl(ownerEmail)))
  }

  def edit(id:String) = StackAction(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    Blogs.findById(id).map { blog =>
      val form = blogForm.fill(BlogData(Some(blog.id), blog.name, blog.alias, blog.description, blog.image, blog.logo, blog.url, blog.disqus, blog.googleAnalytics, blog.useAvatarAsLogo, blog.status))
      Logger.info("form: "+form)
      Logger.info("form(status)="+form("status").value)
      Logger.info("form(status)="+form("status").value.getClass)
      val ownerEmail = Authors.findById(blog.owner).map { _.email }.orNull
      Ok(views.html.blogs_form(Some(blog), form, loggedIn,  PostAux.avatarUrl(ownerEmail)))
    }.getOrElse (Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found")))
  }

  def save = StackAction(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    blogForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.blogs_form(None, formWithErrors, loggedIn, null)),
      blogData => {
        Logger.info("save gravatar: "+blogData.useAvatarAsLogo)
        Blogs.create(loggedIn, blogData.name, blogData.alias, blogData.description, blogData.image, blogData.logo, blogData.url, blogData.disqus, blogData.googleAnalytics, blogData.useAvatarAsLogo)
        Redirect(routes.BlogsGuestController.index()).flashing("success" -> Messages("blogs.success.created"))
      }
    )
  }

  def update(id:String) = StackAction(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    Blogs.findById(id).map { blog =>
      blogForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.blogs_form(Some(blog), formWithErrors, loggedIn, null)),
        blogData => {
          Logger.info("save gravatar: "+blogData.useAvatarAsLogo)

          Blogs.update(blog, blogData.name, blogData.alias, blogData.description, blogData.image, blogData.logo, blogData.url, blogData.disqus, blogData.googleAnalytics, blogData.useAvatarAsLogo, blogData.status)
          Redirect(routes.BlogsGuestController.index()).flashing("success" -> Messages("blogs.success.updated"))
        }
      )
    }.getOrElse (Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found")))
  }


}
