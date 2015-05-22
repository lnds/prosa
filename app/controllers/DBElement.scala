package controllers


import jp.t2v.lab.play2.stackc.{RequestAttributeKey, RequestWithAttributes, StackableController}
import play.api.Play.current
import play.api.db.slick._
import play.api.mvc.{Controller, Result}

import scala.concurrent.Future


trait DBElement extends StackableController {
  self:Controller =>

  case object DBSessionKey extends RequestAttributeKey[Session]

  abstract override def proceed[A](req:RequestWithAttributes[A])(f:RequestWithAttributes[A] => Future[Result]) : Future[Result] = {
    DB.withSession { implicit session =>
      super.proceed(req.set(DBSessionKey, session))(f)
    }
  }

  implicit def dbSession(implicit req: RequestWithAttributes[_]): Session = req.get(DBSessionKey).get

}
