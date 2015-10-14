package controllers

import javax.inject.Inject
import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.{Blogs, Guest, Visitor}
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import scala.concurrent.ExecutionContext.Implicits.global

class BlogsGuestController @Inject() (val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider) extends Controller  with OptionalAuthElement with AuthConfigImpl with I18nSupport {

  def index(pageNum:Int=0) = AsyncStack { implicit request =>
    val user : Visitor = loggedIn.getOrElse(Guest)
    Blogs.listForVisitor(user, page=pageNum).map { page =>
      Ok(views.html.blogs_index("Blogs", page, user))
    }
  }

}