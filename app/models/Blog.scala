package models

import play.api.db.slick.Config.driver.simple._

case class Blog(
  id:String,
  name:String,
  alias:String,
  description:String,
  image:Option[String],
  url:Option[String],
  owner:String
)

class Blogs(tag:Tag) extends Table[Blog](tag, "Blog") {
  def id = column[String]("id", O.PrimaryKey)
  def name = column[String]("name")
  def alias = column[String]("alias")
  def description = column[String]("description")
  def image = column[String]("image", O.Nullable)
  def url = column[String]("url", O.Nullable)
  def owner = column[String]("owner", O.Length(45, varying = true))

  def * = (id,name,alias,description,image.?, url.?, owner) <> (Blog.tupled, Blog.unapply)
}


object Blogs {

  val blogs = TableQuery[Blogs]

  def list(page: Int = 0, pageSize: Int = 10)(implicit s:Session) : Page[Blog] = {
    val offset = pageSize * page
    val query = (for { blog <- blogs } yield blog).sortBy(_.name.asc).drop(offset).take(pageSize)
    val totalRows = count()
    val result = query.list.map(row => row)
    Page(result, page, offset, totalRows, pageSize)
  }

  def count()(implicit s:Session) : Int = Query(blogs.length).first

}