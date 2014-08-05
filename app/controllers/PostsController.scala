package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models._
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.Controller


object PostsController extends Controller with DBElement with TokenValidateElement with AuthElement with AuthConfigImpl  {

  def index(alias:String, pageNum:Int=0) = StackAction(AuthorityKey -> Writer, IgnoreTokenValidation -> None) { implicit request =>

    Blogs.findByAlias(alias).map { blog =>

      val page = Posts.list(blog, draft = false, page = pageNum)
      Ok(views.html.post_index(blog, page, drafts=false, loggedIn))

    } getOrElse Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))
  }

  val BlogNotFound = Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))
  def PostNotFound(alias:String) = Redirect(routes.PostsGuestController.index(alias)).flashing("error" -> Messages("posts.error.not_found"))

  def drafts(alias:String, pageNum:Int=0) = StackAction(AuthorityKey -> Writer, IgnoreTokenValidation -> None) { implicit request =>
    Blogs.findByAlias(alias).map { blog =>
      val page = Posts.list(blog, draft = true, page = pageNum)
      Ok(views.html.post_index(blog, page, drafts=true, loggedIn))
    } getOrElse BlogNotFound
  }

  case class PostData(image:Option[String], title:String, subtitle:Option[String], content:String, draft:Boolean, publish:Option[Boolean])

  val postForm = Form(
    mapping (
      "image" -> optional(text),
      "title" -> nonEmptyText,
      "subtitle" -> optional(text),
      "content" -> nonEmptyText,
      "draft" -> boolean,
      "publish" -> optional(boolean)
    )(PostData.apply)(PostData.unapply)
  )

  def create(alias:String) = StackAction(AuthorityKey -> Writer, IgnoreTokenValidation -> None) { implicit request =>
    Blogs.findByAlias(alias).map { blog =>
      Ok(views.html.posts_new(blog, postForm, loggedIn))
    } getOrElse BlogNotFound
  }

  def save(alias:String) = StackAction(AuthorityKey -> Writer) { implicit request =>
    Blogs.findByAlias(alias).map { blog =>
      postForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.posts_new(blog, formWithErrors, loggedIn)),
        postData => {
          val post = Posts.create(loggedIn, blog, postData.title, postData.subtitle, postData.content, postData.draft, postData.image)
          Logger.info("post creado: id="+post.id+" draft="+post.draft)
          if (postData.draft)
            Redirect(routes.PostsController.drafts(blog.alias)).flashing("success" -> Messages("posts.success.created"))
          else
            Redirect(routes.PostsGuestController.index(alias)).flashing("success" -> Messages("posts.success.created"))
        }
      )
    } getOrElse BlogNotFound
  }

  def edit(alias:String, id:String) = StackAction(AuthorityKey -> Writer, IgnoreTokenValidation -> None) { implicit request =>
    Logger.info("edit post "+alias+" id="+id)
    Blogs.findByAlias(alias).map { blog =>
      Posts.findById(id).map { post =>
        Logger.info("post.author = " + post.author + " loggedIn.id = " + loggedIn.id)
        if (post.author != loggedIn.id)
          Redirect(routes.PostsGuestController.index(alias)).flashing("error" -> Messages("posts.error.not_found"))
        else {
          val postData = PostData(post.image, post.title, post.subtitle, post.content, post.draft, Some(post.published.isDefined))
          Ok(views.html.posts_edit(blog, post, postForm.fill(postData), loggedIn))
        }
      } getOrElse PostNotFound(alias)
    } getOrElse BlogNotFound
  }

  def update(alias:String, id:String) = StackAction(AuthorityKey -> Writer) { implicit request =>
    Blogs.findByAlias(alias).map { blog =>
      Posts.findById(id).map { post =>
        postForm.bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.posts_new(blog, formWithErrors, loggedIn)),
          postData => {
            Posts.update(post, postData.title, postData.subtitle, postData.content, postData.draft, postData.image, postData.publish.getOrElse(false))
            Redirect(routes.PostsController.edit(alias, id)).flashing("success" -> Messages("posts.success.saved"))
          }
        )
      } getOrElse PostNotFound(alias)
    } getOrElse BlogNotFound
  }

  def delete(alias:String, id:String) = StackAction(AuthorityKey -> Writer) { implicit request =>
    Blogs.findByAlias(alias).map { blog =>
      Posts.findById(id).map { post =>
        Posts.delete(post)
        if (post.draft)
          Redirect(routes.PostsController.drafts(alias)).flashing("success" -> Messages("posts.success.deleted"))
        else
          Redirect(routes.PostsGuestController.index(alias)).flashing("success" -> Messages("posts.success.deleted"))
      } getOrElse PostNotFound(alias)
    } getOrElse BlogNotFound
  }

  def unpublish(alias:String, id:String) = StackAction(AuthorityKey -> Writer) { implicit request =>
    Blogs.findByAlias(alias).map { blog =>
      Posts.findById(id).map { post =>
        Posts.update(post.copy(draft = true, published = None))
        Redirect(routes.PostsController.edit(alias, id)).flashing("success" -> Messages("posts.success.unpublished"))
      } getOrElse PostNotFound(alias)
    } getOrElse BlogNotFound
  }





}