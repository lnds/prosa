package controllers

import javax.inject.Inject
import jp.t2v.lab.play2.auth.OptionalAuthElement
import models._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{MessagesApi, I18nSupport, Messages}
import play.api.mvc.Controller
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostsGuestController @Inject() (val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider)
  extends Controller  with OptionalAuthElement with AuthConfigImpl   with I18nSupport  {

  val blogNotFound = Redirect(routes.BlogsGuestController.index())
                      .flashing("error" -> Messages("blogs.error.not_found"))

  def postNotFound(alias:String) = Redirect(routes.PostsGuestController.index(alias))
                                    .flashing("error" -> Messages("posts.error.not_found"))

  val indexView = views.html.post_index

  def index(alias:String, pageNum:Int=0) = AsyncStack { implicit request =>
    Blogs.findByAlias(alias).flatMap {
      case None => Future.successful(blogNotFound)
      case Some(blog) =>
        Authors.findById(blog.owner).flatMap { author =>
          Authors.getAvatar(blog.owner).flatMap { avatar =>
            Posts.listForBlog(blog, draft = false, page = pageNum).map { list =>
              Ok(indexView(blog, author, list, drafts = false, loggedIn.getOrElse(Guest), avatar))
            }
          }
        }
    }
  }

  def view(alias:String, year:Int, month:Int, day:Int, slug:String) = AsyncStack {
    implicit request =>
      Blogs.findByAlias(alias).flatMap {
        case None => Future.successful(blogNotFound)
        case Some(blog) =>
          Posts.find(blog, slug, year, month, day).flatMap {
            case None => Future.successful(postNotFound(alias))
            case Some(post) =>
              Authors.findById(blog.owner).map { author =>
                Ok(views.html.posts_view(blog, author, post, loggedIn.getOrElse(Guest)))
              }
          }
      }
  }


  def atom(alias:String) = AsyncStack { implicit request =>
    Blogs.findByAlias(alias).flatMap {
      case None => Future.successful(blogNotFound)
      case Some(blog) =>
        Posts.last(blog, 10).map { list =>
          Ok(views.xml.posts_atom(blog, list))
        }
    }
  }

}
