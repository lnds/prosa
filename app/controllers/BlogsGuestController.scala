package controllers

import javax.inject.Inject

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global

class BlogsGuestController @Inject() (val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider, private val blogsDAO:BlogsDAO, protected override  val authorsDAO: AuthorsDAO)
  extends Controller  with OptionalAuthElement with AuthConfigImpl with I18nSupport {

  def index(pageNum:Int=0) = AsyncStack { implicit request =>
    val user : Visitor = loggedIn.getOrElse(Guest)
    blogsDAO.listForVisitor(user, page=pageNum).map { page =>
      Ok(views.html.blogs_index("Blogs", page, user))
    }
  }

}