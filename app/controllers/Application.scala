package controllers

import javax.inject.Inject
import jp.t2v.lab.play2.auth.OptionalAuthElement
import models.{Posts, Post, Guest}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Application @Inject()(val messagesApi:MessagesApi, dbConfigProvider:DatabaseConfigProvider) extends Controller  with OptionalAuthElement with AuthConfigImpl with I18nSupport {

  val MAX_POSTS = 10

  def index = AsyncStack { implicit request =>
    Posts.last(MAX_POSTS).flatMap { posts =>
      Future.successful(Ok(views.html.index("Prosa", posts, loggedIn.getOrElse(Guest))))
    }
  }

  def untrail(path: String) = AsyncStack { implicit request =>
    Future.successful(MovedPermanently("/" + path))
  }

}