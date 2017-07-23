package controllers


import dal.{AuthorsDAO, BlogsDAO}
import javax.inject.Inject

import jp.t2v.lab.play2.auth.AuthElement
import models._
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller}
import tools.PostAux

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


case class BlogData(id:Option[String], name:String,alias:String,description:String,image:Option[String],
                    logo:Option[String],url:Option[String], disqus:Option[String], googleAnalytics:Option[String],
                    useAvatarAsLogo:Option[Boolean], status:Int, twitter:Option[String],
                    showAds:Option[Boolean], adsCode:Option[String])

class BlogsController @Inject() (val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider, private val blogsDAO : BlogsDAO, override protected val  authorsDAO:AuthorsDAO)
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
      "twitter" -> optional(text),
      "show_ads" -> optional(boolean),
      "ads_code" -> optional(text)
    )
    (BlogData.apply)(BlogData.unapply)
  )

  def checkAliasName(alias:String): Boolean = alias.matches("[a-zA-Z0-9_-]+")   && alias.length() <= 32

  def create: Action[AnyContent] = AsyncStack(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    authorsDAO.findById(loggedIn.id).map {
      case Some(author) =>
        Ok(views.html.blogs_form(None, blogForm, loggedIn, PostAux.avatarUrl(author.email)))
      case None =>
        Redirect(routes.BlogsGuestController.index())
    }
  }

  def edit(id:String): Action[AnyContent] = AsyncStack(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    Logger.info("EDIT")
    blogsDAO.findById(id).flatMap {
      case None =>
        Future.successful(Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found")))
      case Some(blog) =>
        val form = blogForm.fill(BlogData(Some(blog.id), blog.name, blog.alias, blog.description, blog.image, blog.logo,blog.url, blog.disqus, blog.googleAnalytics, blog.useAvatarAsLogo, blog.status.id, blog.twitter, blog.showAds, blog.adsCode))
        authorsDAO.findById(blog.owner).map {
          case None =>
            Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))
          case Some(owner) =>
            Ok(views.html.blogs_form(Some(blog), form, loggedIn,  PostAux.avatarUrl(owner.email)))
        }
    }
  }

  def save: Action[AnyContent] = AsyncStack(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    blogForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.info("FORM WITH ERROR: "+formWithErrors)
        Future.successful(BadRequest(views.html.blogs_form(None, formWithErrors, loggedIn, None)))
      },
      blogData =>
        blogsDAO.findByAlias(blogData.alias).flatMap {
          case None =>
            blogsDAO.create(loggedIn, blogData.name, blogData.alias, blogData.description, blogData.image, blogData.logo, blogData.url, blogData.disqus, blogData.googleAnalytics, blogData.useAvatarAsLogo, blogData.twitter, blogData.showAds, blogData.adsCode).map { _ =>
              Redirect(routes.BlogsGuestController.index()).flashing("success" -> Messages("blogs.success.created"))
            }
          case Some(blog) =>
            Future.successful(BadRequest(views.html.blogs_form(None, blogForm.fill(blogData).withGlobalError("blg"), loggedIn, None)))
        }
    )
  }

  def update(id:String): Action[AnyContent] = AsyncStack(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    blogsDAO.findById(id).map {
      case Some(blog) =>
        blogForm.bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.blogs_form(Some(blog), formWithErrors, loggedIn, None)),
          blogData => {
            blogsDAO.update(blog, blogData.name, blogData.alias, blogData.description, blogData.image, blogData.logo, blogData.url, blogData.disqus, blogData.googleAnalytics, blogData.useAvatarAsLogo, blogData.twitter, blogData.showAds, blogData.adsCode, BlogStatus(blogData.status))
            Redirect(routes.BlogsGuestController.index()).flashing("success" -> Messages("blogs.success.updated"))
          }
        )
      case None =>
        Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))
    }
  }

}
