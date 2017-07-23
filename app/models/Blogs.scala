package models

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import slick.driver.PostgresDriver.api._
import slick.lifted.ProvenShape
import tools.IdGenerator


object BlogStatus extends Enumeration {

  val CREATED = Value(0, "blog.status.created")
  val PUBLISHED = Value(1, "blog.status.published")
  val INACTIVE = Value(-1, "blog.status.published")// <- reserved for administator

  implicit val blogStatusMapper = MappedColumnType.base[BlogStatus.Value, Int](
    s => s.id,
    i => BlogStatus.apply(i)
  )
}

case class Blog(
                 id:String,
                 name:String,
                 alias:String,
                 description:String,
                 image:Option[String],
                 logo:Option[String],
                 url:Option[String],
                 useAvatarAsLogo:Option[Boolean],
                 disqus:Option[String],
                 googleAnalytics:Option[String],
                 status:BlogStatus.Value, //
                 owner:String,
                 twitter:Option[String],
                 showAds:Option[Boolean],
                 adsCode:Option[String]
               ) extends Identifiable

