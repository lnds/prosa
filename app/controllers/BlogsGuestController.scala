package controllers


import dal.{AuthorsDAO, BlogsDAO}
import javax.inject.Inject

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models._
import org.webjars.play.RequireJS
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller}
import scala.concurrent.ExecutionContext


class BlogsGuestController @Inject() (val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider,
                                      private val blogsDAO:BlogsDAO,  val authorsDAO: AuthorsDAO,
                                      implicit val webJarAssets: WebJarAssets, implicit val requireJS: RequireJS,
                                      implicit val ec:ExecutionContext)
  extends Controller  with OptionalAuthElement with AuthConfigImpl with I18nSupport {

  def index(pageNum:Int=0): Action[AnyContent] = AsyncStack { implicit request =>
    val user : Visitor = loggedIn.getOrElse(Guest)
    blogsDAO.listForVisitor(user, page=pageNum).map { page =>
      Ok(views.html.blogs_index("Blogs", page, user))
    }
  }

}