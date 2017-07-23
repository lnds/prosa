package controllers

import models.{Blog, BlogsDAO, Post, PostsDAO}
import play.api.i18n.Messages
import play.api.mvc.Result
import views.html.post_index

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by ediaz on 7/23/17.
  */
trait WithPostController extends WithBlogController {

  val indexView: post_index.type = views.html.post_index

  def postNotFound(alias: String): Result = Redirect(routes.PostsGuestController.index(alias)).flashing("error" -> Messages("posts.error.not_found"))

  def withPost(blogsDAO: BlogsDAO, postsDAO: PostsDAO, alias: String, id: String)(f: (Blog, Post) => Result): Future[Result] =
    withBlog(blogsDAO, alias) { blog =>
      postsDAO.findById(id).map {
        case None => postNotFound(alias)
        case Some(post) =>
          f(blog, post)
      }
    }
}
