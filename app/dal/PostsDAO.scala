package dal


import com.github.tototoshi.slick.PostgresJodaSupport._
import javax.inject.{Inject, Singleton}

import models._
import org.joda.time.{DateTime, Period}
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import slick.lifted.ProvenShape
import tools.IdGenerator



/**
  * Created by ediaz on 7/23/17.
  */



@Singleton
class PostsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, private val blogsDAO: BlogsDAO, private val authorsDAO: AuthorsDAO)(implicit executionContext: ExecutionContext)
  extends DbService[Post]{

  import driver.api._

  class Posts(tag:Tag) extends Table[Post](tag, "post") with HasId {

    def id: Rep[String] = column[String]("id", O.PrimaryKey)
    def blog: Rep[String] = column[String]("blog", O.Length(45, varying = true))
    def image: Rep[Option[String]] = column[Option[String]]("image")
    def title: Rep[String] = column[String]("title")
    def subtitle: Rep[Option[String]] = column[Option[String]]("subtitle")
    def content: Rep[String] = column[String]("content")
    def slug: Rep[Option[String]] = column[Option[String]]("slug")
    def draft: Rep[Boolean] = column[Boolean]("draft")
    def created: Rep[Option[DateTime]] = column[Option[DateTime]]("created")
    def published: Rep[Option[DateTime]] = column[Option[DateTime]]("published")
    def author: Rep[String] = column[String]("author", O.Length(45, varying = true))

    def * : ProvenShape[Post] = (id,blog,image,title,subtitle,content,slug,draft,created,published,author) <> (Post.tupled, Post.unapply)
  }

  type EntityType = Posts

  val items: TableQuery[Posts] = TableQuery[Posts]
  lazy val posts: TableQuery[Posts] = items

  def last(n:Int) : Future[Seq[(Post,Blog)]] = {
    val q = (for {(p,b) <- posts join blogsDAO.blogs on (_.blog === _.id)
                  if p.draft === false && b.status === BlogStatus.PUBLISHED} yield (p,b)
      ).sortBy { case (post, _) => post.published.desc }.take(n)
    dbConfig.db.run(q.result)
  }

  def last(blog:Blog, n:Int) : Future[Seq[(Post,Author)]] = {
    val q = (for {(p,a) <- posts join authorsDAO.authors on (_.author === _.id)
                  if p.blog === blog.id && p.draft === false } yield (p,a)
      ).sortBy { case (post, _) => post.published.desc }.take(n)
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
    insert(post).map { _ => post }
  }

  def update(post:Post, title:String, subtitle:Option[String], content:String, draft:Boolean, image:Option[String], publish:Boolean) : Future[Post] = {
    def published = if (publish) { post.published orElse Some(new DateTime()) }  else None
    def slug = if (publish) { post.slug orElse  tools.PostAux.slugify(title) } else None
    def isDraft = !publish
    val newPost =  post.copy(title=title, subtitle=subtitle, content=content, draft=isDraft, image=image, published=published, slug=slug)
    update(newPost).map { _ =>
      newPost
    }
  }

}