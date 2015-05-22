package models

import play.api.db.slick.Config.driver.simple._
import play.api.i18n.Messages
import services.AuthorService

object BlogStatus extends Enumeration {

  val CREATED = Value(0, Messages("blog.status.created"))
  val PUBLISHED = Value(1, Messages("blog.status.published"))
  val INACTIVE = Value(-1, Messages("blog.status.published"))// <- reserved for administator

  implicit val BlogStatusMapper = MappedColumnType.base[BlogStatus.Value, Int](
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
  owner:String
) extends Identifiable {

  def author(implicit s:Session) = AuthorService.findById(owner)

}

class BlogEntity(tag:Tag) extends Table[Blog](tag, "blog") with HasId {

  def id = column[String]("id", O.PrimaryKey)
  def name = column[String]("name")
  def alias = column[String]("alias")
  def description = column[String]("description")
  def image = column[String]("image", O.Nullable)
  def logo = column[String]("logo", O.Nullable)
  def url = column[String]("url", O.Nullable)
  def useAvatarAsLogo = column[Boolean]("use_avatar_as_logo", O.Nullable)
  def disqus = column[String]("disqus", O.Nullable)
  def googleAnalytics = column[String]("google_analytics", O.Nullable)
  def status = column[BlogStatus.Value]("status")
  def owner = column[String]("owner", O.Length(45, varying = true))

  def * = (id,name,alias,description,image.?, logo.?, url.?, useAvatarAsLogo.?, disqus.?, googleAnalytics.?, status, owner) <> (Blog.tupled, Blog.unapply)
}




