package models

import slick.driver.PostgresDriver.api._
import tools.IdGenerator

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


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


class Blogs(tag:Tag) extends Table[Blog](tag, "blog") with HasId {

  def id = column[String]("id", O.PrimaryKey)
  def name = column[String]("name")
  def alias = column[String]("alias")
  def description = column[String]("description")
  def image = column[Option[String]]("image")
  def logo = column[Option[String]]("logo")
  def url = column[Option[String]]("url")
  def useAvatarAsLogo = column[Option[Boolean]]("use_avatar_as_logo")
  def disqus = column[Option[String]]("disqus")
  def googleAnalytics = column[Option[String]]("google_analytics")
  def status = column[BlogStatus.Value]("status")
  def owner = column[String]("owner", O.Length(45, varying = true))
  def twitter = column[Option[String]]("twitter_handle")
  def showAds = column[Option[Boolean]]("show_ads")
  def adsCode = column[Option[String]]("ads_code")

  def * = (id,name,alias,description,image, logo, url, useAvatarAsLogo, disqus, googleAnalytics, status, owner, twitter, showAds, adsCode) <> (Blog.tupled, Blog.unapply)
}


object Blogs extends DbService[Blog]{

  type EntityType = Blogs

  val items = TableQuery[Blogs]

  lazy val blogs = items

  def listForVisitor(user:Visitor, page: Int = 0, pageSize: Int = 10) : Future[Page[Blog]] = {
    val offset = pageSize * page
    val query = (for { blog <- blogs if blog.status === BlogStatus.PUBLISHED || user.isInstanceOf[Author]} yield blog).sortBy(_.name.asc).drop(offset).take(pageSize)
    val totalRows = count
    val result = dbConfig.db.run(query.result)
    result flatMap (items => totalRows map (rows => Page(items, page, offset, rows, pageSize)))
  }

  def findByAlias(alias:String) : Future[Option[Blog]] =
    dbConfig.db.run(blogs.filter(_.alias === alias).result.headOption)

  def create(owner:Author, name:String,alias:String,description:String,image:Option[String],logo:Option[String],url:Option[String], disqus:Option[String], googleAnalytics:Option[String], useAvatarAsLogo:Option[Boolean], twitter:Option[String], showAds:Option[Boolean], adsCode:Option[String]) : Future[Int] =
    insert(Blog(IdGenerator.nextId(classOf[Blog]), name, alias, description, image, logo, url, useAvatarAsLogo, disqus, googleAnalytics, BlogStatus.CREATED, owner.id, twitter, showAds, adsCode))

  def update(blog:Blog, name:String,alias:String,description:String,image:Option[String],logo:Option[String],url:Option[String], disqus:Option[String], googleAnalytics:Option[String], useAvatarAsLogo:Option[Boolean], twitter:Option[String], showAds:Option[Boolean], adsCode:Option[String], status:BlogStatus.Value) : Future[Int] =
    update (blog.copy(name=name, alias=alias,  useAvatarAsLogo=useAvatarAsLogo, description=description, image=image, logo=logo, url=url, disqus=disqus, googleAnalytics=googleAnalytics,  showAds=showAds, adsCode=adsCode, status=status))

}
