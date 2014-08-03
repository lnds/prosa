package models

import java.sql.Timestamp
import org.joda.time.{Period, DateTime}
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
)  {

  def publishedDate() = {
    published match {
      case None => null
      case Some(time) => new DateTime(time)
    }
  }
}

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

  def last(blog:Blog, n:Int)(implicit s:Session) : List[(Post,Author)] = {
    val q = (for { (p,a) <- posts innerJoin Authors.authors on (_.author === _.id) if p.blog === blog.id } yield (p,a) ).sortBy(_._1.published.desc).take(n)
    q.list.map(p => p)
  }

  def list(blog:Blog, draft:Boolean, page:Int=0, pageSize:Int=10)(implicit s:Session) : Page[Post] = {
    val offset = pageSize * page
    val query = (for { p <- posts if (p.blog === blog.id) && (p.draft === draft) } yield p).sortBy(_.created.desc).drop(offset).take(pageSize)
    val totalRows = count(blog, draft)
    val result = query.list.map(row => row)
    Page(result, page, offset, totalRows, pageSize)
  }

  def count(blog:Blog, draft:Boolean)(implicit s:Session) = Query((for { p <- posts if (p.blog === blog.id) && (p.draft === draft) } yield p).length).first

  def find(blog:Blog, slug:String, year:Int, month:Int, day:Int)(implicit s:Session) = {
    val ini = new DateTime(year, month, day, 0, 0)
    val end = ini.plus(Period.days(1))
    val tini = new Timestamp(ini.getMillis)
    val tend = new Timestamp(end.getMillis)

    val query = for {p <- posts if (p.blog === blog.id) && (p.published >= tini) && (p.published <= tend) && (p.slug === slug)} yield p
    query.firstOption
  }

}