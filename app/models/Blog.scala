package models

import slick.driver.PostgresDriver.api._


object BlogStatus extends Enumeration {

  val CREATED = Value(0, "blog.status.created")
  val PUBLISHED = Value(1, "blog.status.published")
  val INACTIVE = Value(-1, "blog.status.published")// <- reserved for administator

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
) extends Identifiable



