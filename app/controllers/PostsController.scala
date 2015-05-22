package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.Controller
import services.{AuthorService, BlogService, PostService}
import tools.PostAux

import scala.concurrent.Future

object PostsController extends Controller with DBElement with TokenValidateElement with AuthElement with AuthConfigImpl  {

  def index(alias:String, pageNum:Int=0) = AsyncStack(AuthorityKey -> Writer, IgnoreTokenValidation -> None) { implicit request =>

    BlogService.findByAlias(alias).map { blog =>
      val page = PostService.list(blog, draft = false, page = pageNum)
      val ownerEmail = AuthorService.findById(blog.owner).map { _.email }.orNull
      Future.successful(Ok(views.html.post_index(blog, blog.author, page, drafts=false, loggedIn, PostAux.avatarUrl(ownerEmail))))
    } getOrElse Future.successful(Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found")))
  }

  val BlogNotFound = Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))

  def PostNotFound(alias:String) = Redirect(routes.PostsGuestController.index(alias)).flashing("error" -> Messages("posts.error.not_found"))

  def drafts(alias:String, pageNum:Int=0) = AsyncStack(AuthorityKey -> Writer, IgnoreTokenValidation -> None) { implicit request =>
    BlogService.findByAlias(alias).map { blog =>
      val page = PostService.list(blog, draft = true, page = pageNum)
      Future.successful(Ok(views.html.post_index(blog, blog.author, page, drafts=true, loggedIn, PostAux.avatarUrl(loggedIn.email))))
    } getOrElse Future.successful(BlogNotFound)
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

  def create(alias:String) = AsyncStack(AuthorityKey -> Writer, IgnoreTokenValidation -> None) { implicit request =>
    BlogService.findByAlias(alias).map { blog =>
      Future.successful(Ok(views.html.posts_new(blog, postForm, loggedIn)))
    } getOrElse Future.successful(BlogNotFound)
  }

  def save(alias:String) = AsyncStack(AuthorityKey -> Writer) { implicit request =>
    BlogService.findByAlias(alias).map { blog =>
      postForm.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(views.html.posts_new(blog, formWithErrors, loggedIn))),
        postData => {
          val post = PostService.create(loggedIn, blog, postData.title, postData.subtitle, postData.content, postData.draft, postData.image)
          if (postData.draft)
            Future.successful(Redirect(routes.PostsController.drafts(blog.alias)).flashing("success" -> Messages("posts.success.created")))
          else
            Future.successful(Redirect(routes.PostsGuestController.index(alias)).flashing("success" -> Messages("posts.success.created")))
        }
      )
    } getOrElse Future.successful(BlogNotFound)
  }

  def edit(alias:String, id:String) = AsyncStack(AuthorityKey -> Writer, IgnoreTokenValidation -> None) { implicit request =>
    BlogService.findByAlias(alias).map { blog =>
      PostService.findById(id).map { post =>
        if (post.author != loggedIn.id)
          Future.successful(Redirect(routes.PostsGuestController.index(alias)).flashing("error" -> Messages("posts.error.not_found")))
        else {
          val postData = PostData(post.image, post.title, post.subtitle, post.content, post.draft, Some(post.published.isDefined))
          Future.successful(Ok(views.html.posts_edit(blog, post, postForm.fill(postData), loggedIn)))
        }
      } getOrElse Future.successful(PostNotFound(alias))
    } getOrElse Future.successful(BlogNotFound)
  }

  def update(alias:String, id:String) = AsyncStack(AuthorityKey -> Writer) { implicit request =>
    BlogService.findByAlias(alias).map { blog =>
      PostService.findById(id).map { post =>
        postForm.bindFromRequest.fold(
          formWithErrors => Future.successful(BadRequest(views.html.posts_new(blog, formWithErrors, loggedIn))),
          postData => {
            PostService.update(post, postData.title, postData.subtitle, postData.content, postData.draft, postData.image, postData.publish.getOrElse(false))
            Future.successful(Redirect(routes.PostsController.edit(alias, id)).flashing("success" -> Messages("posts.success.saved")))
          }
        )
      } getOrElse Future.successful(PostNotFound(alias))
    } getOrElse Future.successful(BlogNotFound)
  }

  def delete(alias:String, id:String) = AsyncStack(AuthorityKey -> Writer) { implicit request =>
    BlogService.findByAlias(alias).map { blog =>
      PostService.findById(id).map { post =>
        PostService.delete(post)
        if (post.draft)
          Future.successful(Redirect(routes.PostsController.drafts(alias)).flashing("success" -> Messages("posts.success.deleted")))
        else
          Future.successful(Redirect(routes.PostsGuestController.index(alias)).flashing("success" -> Messages("posts.success.deleted")))
      } getOrElse Future.successful(PostNotFound(alias))
    } getOrElse Future.successful(BlogNotFound)
  }

  def unpublish(alias:String, id:String) = AsyncStack(AuthorityKey -> Writer) { implicit request =>
    BlogService.findByAlias(alias).map { blog =>
      PostService.findById(id).map { post =>
        PostService.update(post.copy(draft = true, published = None))
        Future.successful(Redirect(routes.PostsController.edit(alias, id)).flashing("success" -> Messages("posts.success.unpublished")))
      } getOrElse Future.successful(PostNotFound(alias))
    } getOrElse Future.successful(BlogNotFound)
  }

}