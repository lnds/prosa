package controllers

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.Guest
import play.api.mvc._
import services.PostService

object Application extends Controller with DBElement  with OptionalAuthElement with AuthConfigImpl {

  val MAX_POSTS = 10

  def index = StackAction { implicit request =>
    Ok(views.html.index("Prosa",  PostService.last(MAX_POSTS), loggedIn.getOrElse(Guest)))
  }

  def untrail(path: String) = Action {
    MovedPermanently("/" + path)
  }

}