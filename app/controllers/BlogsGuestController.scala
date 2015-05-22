package controllers

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.{Guest, Visitor}
import play.api.mvc.Controller
import services.BlogService
import scala.concurrent.Future

object BlogsGuestController extends Controller with DBElement with OptionalAuthElement with AuthConfigImpl {

  def index(pageNum:Int=0) = AsyncStack { implicit request =>
    val user : Visitor = loggedIn.getOrElse(Guest)
    Future.successful(Ok(views.html.blogs_index("Blogs", BlogService.list(user, page = pageNum), user)))
  }

}