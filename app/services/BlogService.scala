package services

import models._
import play.api.db.slick.Config.driver.simple._
import tools.IdGenerator

object BlogService extends DbService[Blog]{

  type EntityType = BlogEntity

  val items = TableQuery[BlogEntity]

  lazy val blogs = items

  def list(user:Visitor, page: Int = 0, pageSize: Int = 10)(implicit s:Session) : Page[Blog] = {
    val offset = pageSize * page
    val query = (for { blog <- blogs if blog.status === BlogStatus.PUBLISHED || user.isInstanceOf[Author]} yield blog).sortBy(_.name.asc).drop(offset).take(pageSize)
    val totalRows = count()
    val result = query.list.map(row => row)
    Page(result, page, offset, totalRows, pageSize)
  }

  def findByAlias(alias:String)(implicit s:Session) : Option[Blog] = blogs.filter(_.alias === alias).firstOption

  def create(owner:Author, name:String,alias:String,description:String,image:Option[String],logo:Option[String],url:Option[String], disqus:Option[String], gogleAnalytics:Option[String], useAvatarAsLogo:Option[Boolean])(implicit s:Session) {
    val blog = Blog(IdGenerator.nextId(classOf[Blog]), name, alias, description, image, logo, url, useAvatarAsLogo, disqus, gogleAnalytics, BlogStatus.CREATED, owner.id)
    insert(blog)
  }

  def update(blog:Blog, name:String,alias:String,description:String,image:Option[String],logo:Option[String],url:Option[String], disqus:Option[String], gogleAnalytics:Option[String], useAvatarAsLogo:Option[Boolean], status:BlogStatus.Value)(implicit s:Session) {
    update (blog.copy(name=name, alias=alias,  useAvatarAsLogo=useAvatarAsLogo, description=description, image=image, logo=logo, url=url, disqus=disqus, googleAnalytics=gogleAnalytics, status=status))
  }

}
