package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{MessagesApi, I18nSupport, Messages}
import play.api.mvc.Controller
import services.{AuthorService, BlogService, PostService}
import tools.PostAux
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostsGuestController @Inject() (val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider)  extends Controller  with OptionalAuthElement with AuthConfigImpl   with I18nSupport  {

  val BlogNotFound = Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))

  val indexView = views.html.post_index

  def index(alias:String, pageNum:Int=0) = AsyncStack { implicit request =>
    BlogService.findByAlias(alias).flatMap {
      case None => Future.successful(BlogNotFound)
      case Some(blog) =>
        AuthorService.findById(blog.owner).flatMap { author =>
          AuthorService.getAvatar(blog.owner).flatMap { avatar =>
            PostService.listForBlog(blog, draft = false, page = pageNum).map { list =>
              Ok(indexView(blog, author, list, drafts = false, loggedIn.getOrElse(Guest), avatar))
            }
          }
        }
    }
  }

  def view(alias:String, year:Int, month:Int, day:Int, slug:String) = AsyncStack { implicit request =>
    BlogService.findByAlias(alias).flatMap {
      case None => Future.successful(BlogNotFound)
      case Some(blog) =>
        PostService.find(blog, slug, year, month, day).flatMap {
          case None => Future.successful(NotFound)
          case Some(post) =>
            AuthorService.findById(blog.owner).map { author =>
              Ok(views.html.posts_view(blog, author, post, loggedIn.getOrElse(Guest)))
            }
        }
    }
  }

  def atom(alias:String) = AsyncStack { implicit request =>
    BlogService.findByAlias(alias).flatMap {
      case None => Future.successful(BlogNotFound)
      case Some(blog) =>
        PostService.last(blog, 10).map { list =>
          Ok(views.xml.posts_atom(blog, list))
        }
    }
  }

}
