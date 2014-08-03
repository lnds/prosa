package models

import java.sql.Timestamp
import play.api.db.slick.Config.driver.simple._

case class Post(
  id:String,
  blog:String, //
  image:Option[String],
  title:String,
  subtitle:Option[String],
  content:String,
  slug:Option[String],
  draft: Boolean,
  created:Option[Timestamp],
  published:Option[Timestamp],
  author:String
)

class Posts(tag:Tag) extends Table[Post](tag, "Post") {
  def id = column[String]("id", O.PrimaryKey)
  def blog = column[String]("blog")
  def image = column[String]("image")
  def title = column[String]("title")
  def subtitle = column[String]("subtitle")
  def content = column[String]("content")
  def slug = column[String]("slug")
  def draft = column[Boolean]("draft")
  def created = column[Timestamp]("created")
  def published = column[Timestamp]("published")
  def author = column[String]("author", O.Length(45, varying = true))

  def * = (id, blog, image.?, title, subtitle.?, content, slug.?, draft, created.?, published.?, author) <> (Post.tupled, Post.unapply)
}


object Posts {

  val posts = TableQuery[Posts]

  def last(n:Int)(implicit s:Session) : List[(Post,Blog)] = {
    val q = (for { (p,b) <- posts innerJoin Blogs.blogs on (_.blog === _.id)} yield (p,b) ).sortBy(_._1.published.desc).take(n)
    q.list.map(p => p)
  }
}