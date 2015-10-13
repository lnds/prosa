package services

import models._
import tools.IdGenerator
import slick.driver.PostgresDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class BlogEntity(tag:Tag) extends Table[Blog](tag, "blog") with HasId {

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

  def * = (id,name,alias,description,image, logo, url, useAvatarAsLogo, disqus, googleAnalytics, status, owner) <> (Blog.tupled, Blog.unapply)
}


object BlogService extends DbService[Blog]{

  type EntityType = BlogEntity

  val items = TableQuery[BlogEntity]

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

  def create(owner:Author, name:String,alias:String,description:String,image:Option[String],logo:Option[String],url:Option[String], disqus:Option[String], gogleAnalytics:Option[String], useAvatarAsLogo:Option[Boolean]) {
    val blog = Blog(IdGenerator.nextId(classOf[Blog]), name, alias, description, image, logo, url, useAvatarAsLogo, disqus, gogleAnalytics, BlogStatus.CREATED, owner.id)
    insert(blog)
  }

  def update(blog:Blog, name:String,alias:String,description:String,image:Option[String],logo:Option[String],url:Option[String], disqus:Option[String], gogleAnalytics:Option[String], useAvatarAsLogo:Option[Boolean], status:BlogStatus.Value) {
    update (blog.copy(name=name, alias=alias,  useAvatarAsLogo=useAvatarAsLogo, description=description, image=image, logo=logo, url=url, disqus=disqus, googleAnalytics=gogleAnalytics, status=status))
  }

}
