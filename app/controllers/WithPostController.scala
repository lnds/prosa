package controllers

import models.{Blog, BlogsDAO, Post, PostsDAO}
import play.api.i18n.Messages
import play.api.mvc.Result

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by ediaz on 7/23/17.
  */
trait WithPostController extends WithBlogController {


  def postNotFound(alias: String) = Redirect(routes.PostsGuestController.index(alias)).flashing("error" -> Messages("posts.error.not_found"))

  def withPost(blogsDAO: BlogsDAO, postsDAO: PostsDAO, alias: String, id: String)(f: (Blog, Post) => Result) =
    withBlog(blogsDAO, alias) { blog =>
      postsDAO.findById(id).map {
        case None => postNotFound(alias)
        case Some(post) =>
          f(blog, post)
      }
    }
}
