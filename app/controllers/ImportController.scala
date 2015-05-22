package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.Editor
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.Controller
import services.{BlogService, PostService}

object ImportController extends Controller with DBElement with TokenValidateElement with AuthElement with AuthConfigImpl {

  val BlogNotFound = Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))


  def importPosts(alias:String) = StackAction(AuthorityKey -> Editor, IgnoreTokenValidation -> None) { implicit request =>
    BlogService.findByAlias(alias).map { blog =>
      Ok(views.html.posts_import(blog, blog.author, loggedIn))
    } getOrElse BlogNotFound
  }

  val fileFormatForm = Form(single("file_format" -> nonEmptyText))

  def loadPosts(alias:String) = StackAction(parse.multipartFormData, AuthorityKey -> Editor) { implicit request =>
    BlogService.findByAlias(alias).map { blog =>
      request.body.file("file").map { file =>
        fileFormatForm.bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.posts_import(blog, blog.author, loggedIn)),
          formOk => {
            PostService.importPosts(loggedIn, blog, file.ref.file, formOk)
            Redirect(routes.PostsController.index(alias)).flashing("success" -> Messages("posts.success.imported"))
          }
        )
      } getOrElse BlogNotFound
    } getOrElse BlogNotFound
  }

}
