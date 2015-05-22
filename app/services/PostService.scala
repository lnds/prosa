package services

import java.io.File
import java.sql.Timestamp

import models._
import org.joda.time.{DateTime, Period}
import play.api.db.slick.Config.driver.simple._
import play.api.libs.json.{JsArray, JsObject, Json}
import tools.IdGenerator

import scala.io.Source


object PostService extends DbService[Post]{

  type EntityType = PostEntity

  val items = TableQuery[PostEntity]
  lazy val posts = items

  def last(n:Int)(implicit s:Session) : List[(Post,Blog)] = {
    val q = (for { (p,b) <- posts innerJoin BlogService.blogs on (_.blog === _.id) if p.draft === false && b.status === BlogStatus.PUBLISHED} yield (p,b) ).sortBy(_._1.published.desc).take(n)
    q.list.map(p => p)
  }

  def last(blog:Blog, n:Int)(implicit s:Session) : List[(Post,Author)] = {
    val q = (for { (p,a) <- posts innerJoin AuthorService.authors on (_.author === _.id) if p.blog === blog.id && p.draft === false } yield (p,a) ).sortBy(_._1.published.desc).take(n)
    q.list.map(p => p)
  }

  def list(blog:Blog, draft:Boolean, page:Int=0, pageSize:Int=10)(implicit s:Session) : Page[Post] = {
    val offset = pageSize * page
    val query = (for { p <- posts if (p.blog === blog.id) && (p.draft === draft) } yield p).sortBy(if (draft) _.created.desc else _.published.desc).drop(offset).take(pageSize)
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

  def create(author:Author, blog:Blog,  title:String, subtitle:Option[String], content:String, draft:Boolean, image:Option[String])(implicit s:Session) = {
    def published = if (draft) None else Some(new Timestamp(DateTime.now.getMillis))
    def slug = if (draft) None else Some(tools.PostAux.slugify(title))
    def post = Post(id=IdGenerator.nextId(classOf[Post]), blog=blog.id, title=title, subtitle=subtitle,
                    image=image, author=author.id,
                    content=content,
                    created=Some(new Timestamp(DateTime.now.getMillis)),
                    published=published,slug=slug,draft=draft )
    insert(post)
    post
  }

  def update(post:Post, title:String, subtitle:Option[String], content:String, draft:Boolean, image:Option[String], publish:Boolean)(implicit s:Session)  {
    def published = if (publish)  Some(post.published.getOrElse(new Timestamp(DateTime.now.getMillis)))  else None
    def slug = if (publish) Some(post.slug.getOrElse(tools.PostAux.slugify(title))) else None
    def isDraft = !publish
    update(post.copy(title=title, subtitle=subtitle, content=content, draft=isDraft, image=image, published=published, slug=slug))
  }

  def importPosts(author:Author, blog:Blog, file:File, format:String)(implicit s:Session) {
    if (format == "ghost")
      importGhostFormat(author, blog, file)
  }

  def importGhostFormat(author:Author, blog:Blog, file:File)(implicit s:Session) {
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
        created = Some(new Timestamp(created.getMillis)),
        published =  Some(new Timestamp(published.getMillis)), slug = Some(slug), draft = false)
      insert(post)
    }

  }

}