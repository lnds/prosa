package controllers

import dal.{AuthorsDAO, PostsDAO}
import javax.inject.Inject

import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.Guest
import org.webjars.play.RequireJS
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject()(val messagesApi:MessagesApi, dbConfigProvider:DatabaseConfigProvider,
                            val postsDAO:PostsDAO, override protected val  authorsDAO:AuthorsDAO,
                            implicit val webJarAssets: WebJarAssets, implicit val requireJS: RequireJS)
  extends Controller with OptionalAuthElement with AuthConfigImpl with I18nSupport {

  val maxPosts = 10

  def index: Action[AnyContent] = AsyncStack { implicit request =>
    postsDAO.last(maxPosts).map { posts =>
      Ok(views.html.index("Prosa", posts, loggedIn.getOrElse(Guest)))
    }
  }

  def untrail(path: String): Action[AnyContent] = AsyncStack { implicit request =>
    Future.successful(MovedPermanently("/" + path))
  }

}