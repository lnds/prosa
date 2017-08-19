package controllers

import dal.BlogsDAO
import models.Blog
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Controller, Result}

import scala.concurrent.{ExecutionContext, Future}


/**
  * Created by ediaz on 7/23/17.
  */
trait WithBlogController extends Controller with I18nSupport {

  protected val blogsDAO: BlogsDAO

  val blogNotFound: Result = Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))

  def withBlog(alias: String)(f: Blog => Future[Result])(implicit ec:ExecutionContext): Future[Result] =
    blogsDAO.findByAlias(alias).flatMap {
      case None => Future.successful(blogNotFound)
      case Some(blog) =>
        f(blog)
    }
}
