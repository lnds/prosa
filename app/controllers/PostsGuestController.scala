package controllers

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models._
import play.api.i18n.Messages
import play.api.mvc.Controller
import services.{AuthorService, BlogService, PostService}
import tools.PostAux

object PostsGuestController extends Controller with DBElement with OptionalAuthElement with AuthConfigImpl  {


  val BlogNotFound = Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))

  def index(alias:String, pageNum:Int=0) = StackAction { implicit request =>

    val user: Visitor = loggedIn.getOrElse(Guest)

    BlogService.findByAlias(alias).map { blog =>
      if (blog.status != BlogStatus.PUBLISHED)
        BlogNotFound
      else {
        val page = PostService.list(blog, draft = false, page = pageNum)
        val ownerEmail = AuthorService.findById(blog.owner).map {
          _.email
        }.orNull
        Ok(views.html.post_index(blog, blog.author, page, drafts = false, user, PostAux.avatarUrl(ownerEmail)))
      }
    } getOrElse BlogNotFound
  }

  def view(alias:String, year:Int, month:Int, day:Int, slug:String) = StackAction { implicit request =>

    val user : Visitor = loggedIn.getOrElse(Guest)

    BlogService.findByAlias(alias).map { blog =>

      PostService.find(blog, slug, year, month, day) match {
        case Some(post) =>Ok(views.html.posts_view(blog, blog.author, post, user))
        case None => NotFound
      }
    } getOrElse BlogNotFound

  }

  def atom(alias:String) = StackAction { implicit request =>
    val user : Visitor = loggedIn.getOrElse(Guest)

    BlogService.findByAlias(alias).map { blog =>
      val page = PostService.last(blog, 10)
      Ok(views.xml.posts_atom(blog, page))
    }  getOrElse BlogNotFound
  }

}
