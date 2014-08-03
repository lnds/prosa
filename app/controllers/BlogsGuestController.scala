package controllers

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.{Blogs, Guest, Visitor}
import play.api.mvc.Controller

object BlogsGuestController extends Controller with DBElement with OptionalAuthElement with AuthConfigImpl {

  def index(pageNum:Int=0) = StackAction { implicit request =>
    val maybeUser : Option[User] = loggedIn
    val user : Visitor = maybeUser.getOrElse(Guest)
    val page = Blogs.list(page = pageNum)
    Ok(views.html.blogs_index("Blogs", page, user))
  }


}