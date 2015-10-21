package models

import java.io.File
import org.joda.time.{DateTime, Period}
import play.api.libs.json.{JsArray, JsObject, Json}
import slick.driver.PostgresDriver.api._
import tools.IdGenerator
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import com.github.tototoshi.slick.PostgresJodaSupport._


case class Post(id:String,
                blog:String,
                image:Option[String],
                title:String,
                subtitle:Option[String],
                content:String,
                slug:Option[String],
                draft:Boolean,
                created:Option[DateTime],
                published:Option[DateTime],
                author:String) extends Identifiable



class Posts(tag:Tag) extends Table[Post](tag, "post") with HasId {

  def id = column[String]("id", O.PrimaryKey)
  def blog = column[String]("blog", O.Length(45, varying = true))
  def image = column[Option[String]]("image")
  def title = column[String]("title")
  def subtitle = column[Option[String]]("subtitle")
  def content = column[String]("content")
  def slug = column[Option[String]]("slug")
  def draft = column[Boolean]("draft")
  def created = column[Option[DateTime]]("created")
  def published = column[Option[DateTime]]("published")
  def author = column[String]("author", O.Length(45, varying = true))

  def * = (id,blog,image,title,subtitle,content,slug,draft,created,published,author) <> (Post.tupled, Post.unapply)
}

object Posts extends DbService[Post]{

  type EntityType = Posts

  val items = TableQuery[Posts]
  lazy val posts = items

  def last(n:Int) : Future[Seq[(Post,Blog)]] = {
    val q = (for {(p,b) <- posts join Blogs.blogs on (_.blog === _.id)
                  if p.draft === false && b.status === BlogStatus.PUBLISHED} yield (p,b)
            ).sortBy(_._1.published.desc).take(n)
    dbConfig.db.run(q.result)
  }

  def last(blog:Blog, n:Int) : Future[Seq[(Post,Author)]] = {
    val q = (for {(p,a) <- posts join Authors.authors on (_.author === _.id)
                  if p.blog === blog.id && p.draft === false } yield (p,a)
            ).sortBy(_._1.published.desc).take(n)
    dbConfig.db.run(q.result)
  }

  def listForBlog(blog:Blog, draft:Boolean, page:Int=0, pageSize:Int=10) : Future[Page[Post]] = {
    val offset = pageSize * page
    val query = (for { p <- posts if (p.blog === blog.id) && (p.draft === draft) } yield p)
                  .sortBy(if (draft) _.created.desc else _.published.desc).drop(offset).take(pageSize)
    val totalRows = count(blog, draft)
    val result = dbConfig.db.run(query.result)
    result flatMap (items => totalRows map (rows => Page(items, page, offset, rows, pageSize)))
  }

  def count(blog:Blog, draft:Boolean) : Future[Int] =
    dbConfig.db.run((for { p <- posts if (p.blog === blog.id) && (p.draft === draft) } yield p).length.result)

  def find(blog:Blog, slug:String, year:Int, month:Int, day:Int) : Future[Option[Post]] = {
    val ini = new DateTime(year, month, day, 0, 0)
    val end = ini.plus(Period.days(1))

    val query = for {p <- posts
                     if (p.blog === blog.id) && (p.published >= ini) && (p.published <= end) && (p.slug === slug)
                } yield p
    dbConfig.db.run(query.result.headOption)
  }

  def create(author:Author, blog:Blog,  title:String, subtitle:Option[String], content:String, draft:Boolean, image:Option[String]) : Future[Post] = {
    def published = if (draft) None else Some(new DateTime())
    def slug = if (draft) None else tools.PostAux.slugify(title)
    def post = Post(id=IdGenerator.nextId(classOf[Post]), blog=blog.id, title=title, subtitle=subtitle,
                    image=image, author=author.id,
                    content=content,
                    created=Some(new DateTime()),
                    published=published,slug=slug,draft=draft )
    insert(post).map { i => post }
  }

  def update(post:Post, title:String, subtitle:Option[String], content:String, draft:Boolean, image:Option[String], publish:Boolean) : Future[Post] = {
    def published = if (publish) { post.published orElse Some(new DateTime()) }  else None
    def slug = if (publish) { post.slug orElse  tools.PostAux.slugify(title) } else None
    def isDraft = !publish
    val newPost =  post.copy(title=title, subtitle=subtitle, content=content, draft=isDraft, image=image, published=published, slug=slug)
    update(newPost).map { i =>
       newPost
    }
  }

  def importPosts(author:Author, blog:Blog, file:File, format:String) : Unit = {
    if (format == "ghost")
      importGhostFormat(author, blog, file)
  }

  def importGhostFormat(author:Author, blog:Blog, file:File) : Unit = {
    val data = Source.fromFile(file)(scala.io.Codec.UTF8).mkString
    val json = Json.parse(data)
    val jsonPosts = (json \ "data" \ "posts").as[JsArray]
    for (p <- jsonPosts.value) {
      val jp = p.as[JsObject]
      val title = (jp \ "title").as[String]
      val slug = (jp \ "slug").as[String]
      val created = DateTime.parse((jp \ "created_at").as[String])
      val published = DateTime.parse((jp \ "published_at").as[String])
      val html = (jp \ "html").as[String]

      def post = Post(id = IdGenerator.nextId(classOf[Post]), blog = blog.id, title = title, subtitle = None,
                      image = None, author = author.id, content = html,
                      created = Some(created),
                      published =  Some(published), slug = Some(slug), draft = false)
      insert(post)
    }

  }

}