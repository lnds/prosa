package models

import play.api.db.slick.Config.driver.simple._
import play.api.i18n.Messages
import services.AuthorService
import tools.IdGenerator


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
)  {

  def author(implicit s:Session) = AuthorService.findById(owner)

}

class Blogs(tag:Tag) extends Table[Blog](tag, "blog") {
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


object Blogs {

  val blogs = TableQuery[Blogs]

  def list(user:Visitor, page: Int = 0, pageSize: Int = 10)(implicit s:Session) : Page[Blog] = {
    val offset = pageSize * page
    val query = (for { blog <- blogs if blog.status === BlogStatus.PUBLISHED || user.isInstanceOf[Author]} yield blog).sortBy(_.name.asc).drop(offset).take(pageSize)
    val totalRows = count()
    val result = query.list.map(row => row)
    Page(result, page, offset, totalRows, pageSize)
  }

  def count()(implicit s:Session) : Int = Query(blogs.length).first

  def findById(id:String)(implicit s:Session) = blogs.filter(_.id === id).firstOption

  def findByAlias(alias:String)(implicit s:Session) = blogs.filter(_.alias === alias).firstOption

  def create(owner:Author, name:String,alias:String,description:String,image:Option[String],logo:Option[String],url:Option[String], disqus:Option[String], gogleAnalytics:Option[String], useAvatarAsLogo:Option[Boolean])(implicit s:Session) {
    val blog = Blog(IdGenerator.nextId(classOf[Blog]), name, alias, description, image, logo, url, useAvatarAsLogo, disqus, gogleAnalytics, BlogStatus.CREATED, owner.id)
    insert(blog)
  }

  def update(blog:Blog, name:String,alias:String,description:String,image:Option[String],logo:Option[String],url:Option[String], disqus:Option[String], gogleAnalytics:Option[String], useAvatarAsLogo:Option[Boolean], status:BlogStatus.Value)(implicit s:Session) {
    update (blog.copy(name=name, alias=alias,  useAvatarAsLogo=useAvatarAsLogo, description=description, image=image, logo=logo, url=url, disqus=disqus, googleAnalytics=gogleAnalytics, status=status))
  }

  def insert(blog:Blog)(implicit s:Session) {
    blogs.insert(blog)
  }

  def update(blog:Blog)(implicit s:Session) {
    blogs.filter(_.id===blog.id).update(blog)
  }

  def delete(blog:Blog)(implicit s:Session): Unit = {
    blogs.filter(_.id === blog.id).delete
  }
}
