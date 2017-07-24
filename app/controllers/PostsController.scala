package controllers


import dal.{AuthorsDAO, BlogsDAO, PostsDAO}
import javax.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import tools.PostAux

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


case class PostData(image:Option[String], title:String, subtitle:Option[String], content:String, draft:Boolean, publish:Option[Boolean])

class PostsController  @Inject() (val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider,
                                  override protected val postsDAO: PostsDAO, override protected val blogsDAO: BlogsDAO,
                                  override protected val  authorsDAO:AuthorsDAO)
  extends  WithPostController with TokenValidateElement with AuthElement with AuthConfigImpl  {

  def index(alias: String, pageNum: Int = 0): Action[AnyContent] = AsyncStack(AuthorityKey -> models.Writer, IgnoreTokenValidation -> None) { implicit request =>
    withBlog(alias) { blog =>
      authorsDAO.findById(blog.owner).flatMap { author =>
        authorsDAO.avatar(blog.owner).flatMap { avatar =>
          postsDAO.listForBlog(blog, draft = false, page = pageNum).map { list =>
            Ok(indexView(blog, author, list, drafts = false, loggedIn, avatar))
          }
        }
      }
    }
  }

  def drafts(alias: String, pageNum: Int = 0): Action[AnyContent] = AsyncStack(AuthorityKey -> models.Writer, IgnoreTokenValidation -> None) { implicit request =>
    withBlog(alias) { blog =>
      authorsDAO.findById(blog.owner).flatMap { author =>
        postsDAO.listForBlog(blog, draft = true, page = pageNum).map { list =>
          Ok(indexView(blog, author, list, drafts = true, loggedIn, PostAux.avatarUrl(loggedIn.email)))
        }
      }
    }
  }

  val postForm = Form(
    mapping(
      "image" -> optional(text),
      "title" -> nonEmptyText,
      "subtitle" -> optional(text),
      "content" -> nonEmptyText,
      "draft" -> boolean,
      "publish" -> optional(boolean)
    )(PostData.apply)(PostData.unapply)
  )

  def create(alias: String): Action[AnyContent] = AsyncStack(AuthorityKey -> models.Writer, IgnoreTokenValidation -> None) { implicit request =>
    withBlog(alias) { blog =>
      Future.successful(Ok(views.html.posts_new(blog, postForm, loggedIn)))
    }
  }

  def save(alias: String): Action[AnyContent] = AsyncStack(AuthorityKey -> models.Writer) { implicit request =>
    withBlog(alias) { blog =>
      postForm.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(views.html.posts_new(blog, formWithErrors, loggedIn))),
        postData => {
          postsDAO.create(loggedIn, blog, postData.title, postData.subtitle, postData.content, postData.draft, postData.image).map { post =>
            if (post.draft)
              Redirect(routes.PostsController.drafts(blog.alias)).flashing("success" -> Messages("posts.success.created"))
            else
              Redirect(routes.PostsGuestController.index(alias)).flashing("success" -> Messages("posts.success.created"))
          }
        })
    }
  }

  def edit(alias: String, id: String): Action[AnyContent] = AsyncStack(AuthorityKey -> models.Writer, IgnoreTokenValidation -> None) { implicit request =>
    withPost(alias, id) { (blog, post) =>
      if (post.author == loggedIn.id)
        Ok(views.html.posts_edit(blog, post, postForm.fill(PostData(post.image, post.title, post.subtitle, post.content, post.draft, Some(post.published.isDefined))), loggedIn))
      else
        postNotFound(alias)
    }
  }

  def update(alias: String, id: String): Action[AnyContent] = AsyncStack(AuthorityKey -> models.Writer) { implicit request =>
    withPost(alias, id) { (blog, post) =>
      postForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.posts_new(blog, formWithErrors, loggedIn)),
        postData => {
          postsDAO.update(post, postData.title, postData.subtitle, postData.content, postData.draft, postData.image, postData.publish.getOrElse(false))
          Redirect(routes.PostsController.edit(alias, id)).flashing("success" -> Messages("posts.success.saved"))
        }
      )
    }
  }

  def delete(alias: String, id: String): Action[AnyContent] = AsyncStack(AuthorityKey -> models.Writer) { implicit request =>
    withPost(alias, id) { (_, post) =>
      postsDAO.delete(post.id)
      if (post.draft)
        Redirect(routes.PostsController.drafts(alias)).flashing("success" -> Messages("posts.success.deleted"))
      else
        Redirect(routes.PostsGuestController.index(alias)).flashing("success" -> Messages("posts.success.deleted"))
    }
  }

  def unpublish(alias: String, id: String): Action[AnyContent] = AsyncStack(AuthorityKey -> models.Writer) { implicit request =>
    withPost(alias, id) { (_, post) =>
      postsDAO.update(post.copy(draft = true, published = None))
      Redirect(routes.PostsController.edit(alias, id)).flashing("success" -> Messages("posts.success.unpublished"))
    }
  }

}