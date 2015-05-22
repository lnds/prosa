package controllers

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models._
import play.api.i18n.Messages
import play.api.mvc.Controller
import services.{AuthorService, BlogService, PostService}
import tools.PostAux
import scala.concurrent.Future

object PostsGuestController extends Controller with DBElement with OptionalAuthElement with AuthConfigImpl  {

  val BlogNotFound = Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))

  val indexView = views.html.post_index

  def index(alias:String, pageNum:Int=0) = AsyncStack { implicit request =>
    Future.successful(
      BlogService.findByAlias(alias).filter(blog => blog.status == BlogStatus.PUBLISHED).map { blog =>
          val ownerEmail = AuthorService.findById(blog.owner).map(_.email).orNull
          Ok(indexView(blog, blog.author, PostService.list(blog, draft = false, page = pageNum), drafts = false, loggedIn.getOrElse(Guest), PostAux.avatarUrl(ownerEmail)))
      } getOrElse BlogNotFound
    )
  }

  def view(alias:String, year:Int, month:Int, day:Int, slug:String) = AsyncStack { implicit request =>
    Future.successful(
      BlogService.findByAlias(alias).map { blog =>
        PostService.find(blog, slug, year, month, day).map { post =>
         Ok(views.html.posts_view(blog, blog.author, post, loggedIn.getOrElse(Guest)))
        } getOrElse NotFound
      } getOrElse BlogNotFound
    )
  }

  def atom(alias:String) = AsyncStack { implicit request =>
    Future.successful(
      BlogService.findByAlias(alias).map { blog =>
        Ok(views.xml.posts_atom(blog, PostService.last(blog, 10)))
      }  getOrElse BlogNotFound
    )
  }

}
