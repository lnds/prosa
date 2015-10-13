package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.AuthElement
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import play.api.mvc.Controller
import services.{AuthorService, BlogService, PostService}
import tools.PostAux
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class PostData(image:Option[String], title:String, subtitle:Option[String], content:String, draft:Boolean, publish:Option[Boolean])

class PostsController  @Inject() (val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider) extends Controller  with TokenValidateElement with AuthElement with AuthConfigImpl  with I18nSupport {

  val blogNotFound = Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))

  val indexView = views.html.post_index

  def index(alias:String, pageNum:Int=0) = AsyncStack(AuthorityKey -> models.Writer, IgnoreTokenValidation -> None) { implicit request =>
      BlogService.findByAlias(alias).flatMap {
        case Some(blog) =>
          AuthorService.findById(blog.owner).flatMap { author =>
            AuthorService.getAvatar(blog.owner).flatMap { avatar =>
              PostService.listForBlog(blog, draft = false, page = pageNum).map { list =>
                Ok(indexView(blog, author, list, drafts = false, loggedIn, avatar))
              }
            }
          }
        case None => Future.successful(blogNotFound)
      }
  }

  val BlogNotFound = Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))

  def PostNotFound(alias:String) = Redirect(routes.PostsGuestController.index(alias)).flashing("error" -> Messages("posts.error.not_found"))

  def drafts(alias:String, pageNum:Int=0) = AsyncStack(AuthorityKey -> models.Writer, IgnoreTokenValidation -> None) { implicit request =>
      BlogService.findByAlias(alias).flatMap {
        case Some(blog) =>
          AuthorService.findById(blog.owner).flatMap { author =>
            PostService.listForBlog(blog, draft = true, page = pageNum).map { list =>
             Ok(indexView(blog, author, list, drafts = true, loggedIn, PostAux.avatarUrl(loggedIn.email)))
            }
          }
        case None =>  Future.successful(BlogNotFound)
      }
  }


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

  def create(alias:String) = AsyncStack(AuthorityKey -> models.Writer, IgnoreTokenValidation -> None) { implicit request =>
    BlogService.findByAlias(alias).flatMap {
      case Some(blog) =>
        Future.successful(Ok(views.html.posts_new(blog, postForm, loggedIn)))
      case None => Future.successful(BlogNotFound)
    }
  }

  def save(alias:String) = AsyncStack(AuthorityKey -> models.Writer) { implicit request =>
    BlogService.findByAlias(alias).flatMap {
      case None => Future.successful(BlogNotFound)
      case Some(blog) =>
        postForm.bindFromRequest.fold(
          formWithErrors => Future.successful(BadRequest(views.html.posts_new(blog, formWithErrors, loggedIn))),
          postData => {
            val post = PostService.create(loggedIn, blog, postData.title, postData.subtitle, postData.content, postData.draft, postData.image)
            if (postData.draft)
              Future.successful(Redirect(routes.PostsController.drafts(blog.alias)).flashing("success" -> Messages("posts.success.created")))
            else
              Future.successful(Redirect(routes.PostsGuestController.index(alias)).flashing("success" -> Messages("posts.success.created")))
          })
    }
  }

  def edit(alias:String, id:String) = AsyncStack(AuthorityKey -> models.Writer, IgnoreTokenValidation -> None) { implicit request =>
    BlogService.findByAlias(alias).flatMap {
      case None => Future.successful(BlogNotFound)
      case Some(blog) =>
        PostService.findById(blog.id).map {
          case None => BlogNotFound
          case Some(post) =>
            if (post.author != loggedIn.id)
              Redirect(routes.PostsGuestController.index(alias)).flashing("error" -> Messages("posts.error.not_found"))
            else
             Ok(views.html.posts_edit(blog, post, postForm.fill(PostData(post.image, post.title, post.subtitle, post.content, post.draft, Some(post.published.isDefined))), loggedIn))
        }
    }
  }

  def update(alias:String, id:String) = AsyncStack(AuthorityKey -> models.Writer) { implicit request =>
    BlogService.findByAlias(alias).flatMap {
      case None => Future.successful(BlogNotFound)
      case Some(blog) =>
        PostService.findById(id).map {
          case None => BlogNotFound
          case Some(post) =>
            postForm.bindFromRequest.fold(
              formWithErrors => BadRequest(views.html.posts_new(blog, formWithErrors, loggedIn)),
              postData => {
                PostService.update(post, postData.title, postData.subtitle, postData.content, postData.draft, postData.image, postData.publish.getOrElse(false))
                Redirect(routes.PostsController.edit(alias, id)).flashing("success" -> Messages("posts.success.saved"))
              }
            )
        }
    }
  }

  def delete(alias:String, id:String) = AsyncStack(AuthorityKey -> Writer) { implicit request =>
      BlogService.findByAlias(alias).flatMap {
        case None => Future.successful(BlogNotFound)
        case Some(blog) =>
          PostService.findById(id).map {
            case None => BlogNotFound
            case Some(post) =>
              PostService.delete(post.id)
              if (post.draft)
                Redirect(routes.PostsController.drafts(alias)).flashing("success" -> Messages("posts.success.deleted"))
              else
                Redirect(routes.PostsGuestController.index(alias)).flashing("success" -> Messages("posts.success.deleted"))
          }
      }
  }

  def unpublish(alias:String, id:String) = AsyncStack(AuthorityKey -> models.Writer) { implicit request =>
    BlogService.findByAlias(alias).flatMap {
      case None => Future.successful(BlogNotFound)
      case Some(blog) =>
        PostService.findById(id).map {
          case None => BlogNotFound
          case Some(post) =>
            PostService.update(post.copy(draft = true, published = None))
            Redirect(routes.PostsController.edit(alias, id)).flashing("success" -> Messages("posts.success.unpublished"))
        }
    }
  }

}