package controllers

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.{Blogs, Guest, Visitor}
import play.api.mvc.Controller

object BlogsGuestController extends Controller with DBElement with OptionalAuthElement with AuthConfigImpl {

  def index(pageNum:Int=0) = StackAction { implicit request =>
    val user : Visitor = loggedIn.getOrElse(Guest)
    Ok(views.html.blogs_index("Blogs", Blogs.list(user, page = pageNum), user))
  }

}