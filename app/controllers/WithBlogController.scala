package controllers

import models.{Blog, BlogsDAO}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Controller, Result, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by ediaz on 7/23/17.
  */
trait WithBlogController extends Controller with I18nSupport {

  val blogNotFound: Result = Redirect(routes.BlogsGuestController.index()).flashing("error" -> Messages("blogs.error.not_found"))

  def withBlog(blogsDAO: BlogsDAO, alias: String)(f: Blog => Future[Result]): Future[Result] =
    blogsDAO.findByAlias(alias).flatMap {
      case None => Future.successful(blogNotFound)
      case Some(blog) =>
        f(blog)
    }
}
