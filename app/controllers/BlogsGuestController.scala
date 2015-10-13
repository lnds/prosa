package controllers

import javax.inject.Inject
import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.{Guest, Visitor}
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import services.BlogService
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class BlogsGuestController @Inject() (val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider) extends Controller  with OptionalAuthElement with AuthConfigImpl with I18nSupport {

  def index(pageNum:Int=0) = AsyncStack { implicit request =>
    val user : Visitor = loggedIn.getOrElse(Guest)
    BlogService.listForVisitor(user, page=pageNum).flatMap { page =>
      Future.successful(Ok(views.html.blogs_index("Blogs", page, user)))
    }
  }

}