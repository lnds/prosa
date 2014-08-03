package controllers

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.{Guest, Visitor, Posts, Blogs}
import org.joda.time.{Period, DateTime}
import play.api.i18n.Messages
import play.api.mvc.Controller

object PostsGuestController extends Controller with DBElement with OptionalAuthElement with AuthConfigImpl  {

  def index(alias:String, pageNum:Int=0) = StackAction { implicit request =>

    val user: Visitor = loggedIn.getOrElse(Guest)

    Blogs.findByAlias(alias).map { blog =>

      val page = Posts.list(blog, draft = false, page = pageNum)
      Ok(views.html.post_index(blog, page, false, user))

    } getOrElse Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))
  }

  def viewPost(alias:String, year:Int, month:Int, day:Int, slug:String) = StackAction { implicit request =>

    val user : Visitor = loggedIn.getOrElse(Guest)

    Blogs.findByAlias(alias).map { blog =>

      Posts.find(blog, slug, year, month, day) match {
        case Some(post) =>Ok(views.html.posts_view(blog, post, user))
        case None => NotFound
      }
    } getOrElse Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))

  }

  def atom(alias:String) = StackAction { implicit request =>
    val user : Visitor = loggedIn.getOrElse(Guest)

    Blogs.findByAlias(alias).map { blog =>
      val page = Posts.last(blog, 10)
      Ok(views.xml.posts_atom(blog, page))
    }  getOrElse Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))
  }

}
