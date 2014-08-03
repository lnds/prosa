package controllers

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.Posts
import play.api.mvc._

object Application extends Controller with DBElement  with OptionalAuthElement with AuthConfigImpl {

  def index = StackAction { implicit request =>
    val posts = Posts.last(10)
    Ok(views.html.index("Prosa", posts))
  }

}