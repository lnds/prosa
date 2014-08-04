package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models._
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.Controller


object PostsController extends Controller with DBElement with TokenValidateElement with AuthElement with AuthConfigImpl  {

  def drafts(alias:String, pageNum:Int=0) = StackAction(AuthorityKey -> Writer, IgnoreTokenValidation -> None) { implicit request =>
    Blogs.findByAlias(alias).map { blog =>

      val page = Posts.list(blog, draft = true, page = pageNum)
      Ok(views.html.post_index(blog, page, drafts=true, loggedIn))

    } getOrElse Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))
  }

  case class PostData(image:Option[String], title:String, subtitle:Option[String], content:String, draft:Boolean)

  val postForm = Form(
    mapping (
      "image" -> optional(text),
      "title" -> nonEmptyText,
      "subtitle" -> optional(text),
      "content" -> nonEmptyText,
      "draft" -> boolean
    )(PostData.apply)(PostData.unapply)
  )

  def create(alias:String) = StackAction(AuthorityKey -> Writer, IgnoreTokenValidation -> None) { implicit request =>
    Blogs.findByAlias(alias).map { blog =>
      Ok(views.html.posts_new(blog, postForm, loggedIn))
    } getOrElse Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))
  }

  def save(alias:String) = StackAction(AuthorityKey -> Writer) { implicit request =>
    Blogs.findByAlias(alias).map { blog =>
      postForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.posts_new(blog, formWithErrors, loggedIn)),
        postData => {
          val post = Posts.create(loggedIn, blog, postData.title, postData.subtitle, postData.content, postData.draft, postData.image)
          if (postData.draft)
            Redirect(routes.PostsController.editPost(alias, post.id)).flashing("success" -> Messages("posts.success.created"))
          else
            Redirect(routes.PostsGuestController.index(alias)).flashing("success" -> Messages("posts.success.created"))
        }
      )
    } getOrElse Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))
  }

  def editPost(alias:String, id:String) = StackAction(AuthorityKey -> Writer, IgnoreTokenValidation -> None) { implicit request =>
    Posts.findById(id).map { post =>
      Logger.info("post.author = "+post.author+" loggedIn.id = "+loggedIn.id)
      if (post.author != loggedIn.id)
        Redirect(routes.PostsGuestController.index(alias)).flashing("error" -> Messages("posts.error.not_found"))
      else {
        val postData = PostData(post.image, post.title, post.subtitle, post.content, post.draft)
        Ok(views.html.posts_edit(alias, id, postForm.fill(postData), loggedIn))
      }
    } getOrElse Redirect(routes.PostsGuestController.index(alias)).flashing("error" -> Messages("posts.error.not_found"))
  }

  def deletePost(alias:String, id:String) = TODO

  def unpublishPost(alias:String, id:String) = TODO

  def updatePost(alias:String, id:String) = TODO


}