package controllers

import dal.AuthorsDAO
import jp.t2v.lab.play2.auth.{AuthConfig, CookieTokenAccessor}
import models._
import play.api.mvc.Results._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.{ClassTag, classTag}

trait AuthConfigImpl extends AuthConfig {

  type Id = String
  type User = Author
  type Authority = Permission

  val idTag: ClassTag[Id] = classTag[Id]

  val sessionTimeoutInSeconds = 3600

  val authorsDAO: AuthorsDAO

  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] =
    authorsDAO.findById(id)

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = {
    val uri = request.session.get("access_uri").getOrElse(routes.BlogsGuestController.index().url.toString)
    Future.successful(Redirect(uri).withSession(request.session - "access_uri"))
  }

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = Future.successful(Redirect(routes.Application.index()))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.AuthController.login()).withSession("access_uri" -> request.uri))

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit ctx: ExecutionContext): Future[Result] = {
    Future(Redirect(routes.AuthController.login()))
  }

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] =
    Future.successful((Permission.valueOf(user.permission), authority) match {
    case (Administrator, _) => true
    case (Editor, Editor) => true
    case (Editor, Writer) => true
    case (Writer, Writer) => true
    case _ => false
  })

  override lazy val tokenAccessor = new CookieTokenAccessor(
    cookieSecureOption = false,
    cookieMaxAge = Some(sessionTimeoutInSeconds)
  )
}
