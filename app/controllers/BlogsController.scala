package controllers

import javax.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.{Blogs, Authors, BlogStatus, Editor}
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import play.api.mvc.Controller
import tools.PostAux
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class BlogData(id:Option[String], name:String,alias:String,description:String,image:Option[String],
                    logo:Option[String],url:Option[String], disqus:Option[String], googleAnalytics:Option[String],
                    useAvatarAsLogo:Option[Boolean], status:Int, twitter:Option[String])

class BlogsController @Inject() (val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider)
  extends Controller with TokenValidateElement with AuthElement with AuthConfigImpl with I18nSupport {


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
      "status" -> number(min=BlogStatus.INACTIVE.id, max=BlogStatus.PUBLISHED.id),
      "twitter" -> optional(text)
    )
    (BlogData.apply)(BlogData.unapply)
  )

  def checkAliasName(alias:String) = alias.matches("[a-zA-Z0-9_-]+")   && alias.length() <= 32

  def create = AsyncStack(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    Authors.findById(loggedIn.id).map {
      case Some(author) =>
        Ok(views.html.blogs_form(None, blogForm, loggedIn, PostAux.avatarUrl(author.email)))
      case None =>
        Redirect(routes.BlogsGuestController.index())
    }
  }

  def edit(id:String) = AsyncStack(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    Blogs.findById(id).flatMap {
      case None =>
        Future.successful(Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found")))
      case Some(blog) =>
        val form = blogForm.fill(BlogData(Some(blog.id), blog.name, blog.alias, blog.description, blog.image, blog.logo,blog.url, blog.disqus, blog.googleAnalytics, blog.useAvatarAsLogo, blog.status.id, blog.twitter))
        Authors.findById(blog.owner).map {
          case None =>
            Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))
          case Some(owner) =>
            Ok(views.html.blogs_form(Some(blog), form, loggedIn,  PostAux.avatarUrl(owner.email)))
        }
    }
  }

  def save = AsyncStack(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    blogForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.blogs_form(None, formWithErrors, loggedIn, None))),
      blogData =>
        Blogs.findByAlias(blogData.alias).flatMap {
          case None =>
            Blogs.create(loggedIn, blogData.name, blogData.alias, blogData.description, blogData.image, blogData.logo, blogData.url, blogData.disqus, blogData.googleAnalytics, blogData.useAvatarAsLogo, blogData.twitter).map { i =>
              Redirect(routes.BlogsGuestController.index()).flashing("success" -> Messages("blogs.success.created"))
            }
          case Some(blog) =>
            Future.successful(BadRequest(views.html.blogs_form(None, blogForm.fill(blogData).withGlobalError("blg"), loggedIn, None)))
        }
    )
  }

  def update(id:String) = AsyncStack(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    Blogs.findById(id).map {
      case Some(blog) =>
        blogForm.bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.blogs_form(Some(blog), formWithErrors, loggedIn, None)),
          blogData => {
            Blogs.update(blog, blogData.name, blogData.alias, blogData.description, blogData.image, blogData.logo, blogData.url, blogData.disqus, blogData.googleAnalytics, blogData.useAvatarAsLogo, BlogStatus(blogData.status))
            Redirect(routes.BlogsGuestController.index()).flashing("success" -> Messages("blogs.success.updated"))
          }
        )
      case None =>
        Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))
    }
  }

}
