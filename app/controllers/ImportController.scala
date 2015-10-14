package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.AuthElement
import models.{Posts, Blogs, Authors, Editor}
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import play.api.mvc.Controller
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportController @Inject() (val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider) extends Controller with TokenValidateElement with AuthElement with AuthConfigImpl with I18nSupport {

  val BlogNotFound = Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))


  def importPosts(alias:String) = AsyncStack(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    Blogs.findByAlias(alias).flatMap {
      case Some(blog) =>
        Authors.findById(blog.owner).flatMap { author =>
          Future.successful(Ok(views.html.posts_import(blog, author, loggedIn)))
        }
      case None =>
        Future.successful(BlogNotFound)
    }
  }

  val fileFormatForm = Form(single("file_format" -> nonEmptyText))

  def loadPosts(alias:String) = AsyncStack(parse.multipartFormData, AuthorityKey -> Editor) { implicit request =>
    Blogs.findByAlias(alias).flatMap {
      case Some(blog) =>
        request.body.file("file").map { file =>
          fileFormatForm.bindFromRequest.fold(
            formWithErrors =>
              Authors.findById(blog.owner).flatMap { author =>
                Future.successful(BadRequest(views.html.posts_import(blog, author, loggedIn)))
              },
            formOk => {
              Posts.importPosts(loggedIn, blog, file.ref.file, formOk)
              Future.successful(Redirect(routes.PostsController.index(alias)).flashing("success" -> Messages("posts.success.imported")))
            }
          )
        } getOrElse Future.successful(BlogNotFound)
      case None => Future.successful(BlogNotFound)
    }
  }

}
