package dal

import javax.inject.{Inject, Singleton}

import models._
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import slick.lifted.ProvenShape
import tools.IdGenerator


/**
  * Blogs Data Access Object
  * Created by ediaz on 7/23/17.
  */




@Singleton
class BlogsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends DbService[Blog]{

  import driver.api._

  class Blogs(tag:Tag) extends Table[Blog](tag, "blog") with HasId {

    def id: Rep[String] = column[String]("id", O.PrimaryKey, O.Length(keySize))
    def name: Rep[String] = column[String]("name")
    def alias: Rep[String] = column[String]("alias")
    def description: Rep[String] = column[String]("description")
    def image: Rep[Option[String]] = column[Option[String]]("image")
    def logo: Rep[Option[String]] = column[Option[String]]("logo")
    def url: Rep[Option[String]] = column[Option[String]]("url")
    def useAvatarAsLogo: Rep[Option[Boolean]] = column[Option[Boolean]]("use_avatar_as_logo")
    def disqus: Rep[Option[String]] = column[Option[String]]("disqus")
    def googleAnalytics: Rep[Option[String]] = column[Option[String]]("google_analytics")
    def status: Rep[Int] = column[Int]("status")
    def owner: Rep[String] = column[String]("owner", O.Length(keySize, varying = true))
    def twitter: Rep[Option[String]] = column[Option[String]]("twitter_handle")
    def showAds: Rep[Option[Boolean]] = column[Option[Boolean]]("show_ads")
    def adsCode: Rep[Option[String]] = column[Option[String]]("ads_code")

    def * : ProvenShape[Blog] = (id,name,alias,description,image, logo, url, useAvatarAsLogo, disqus, googleAnalytics, status, owner, twitter, showAds, adsCode) <> (Blog.tupled, Blog.unapply)
  }



  object BlogStatus extends Enumeration  {


    val CREATED = Value(0, "blog.status.created")
    val PUBLISHED = Value(1, "blog.status.published")
    val INACTIVE = Value(-1, "blog.status.inactive")// <- reserved for administator

    implicit val blogStatusMapper = MappedColumnType.base[BlogStatus.Value, Int](
      s => s.id,
      i => BlogStatus.apply(i)
    )
  }

  type EntityType = Blogs

  val items: TableQuery[Blogs] = TableQuery[Blogs]

  lazy val blogs: TableQuery[Blogs] = items

  def listForVisitor(user:Visitor, page: Int = 0, pageSize: Int = 10) : Future[Page[Blog]] = {
    val offset = pageSize * page
    val query = (for { blog <- blogs if blog.status === BlogStatus.PUBLISHED.id || user.isInstanceOf[Author]} yield blog)
                .sortBy(_.name.asc).drop(offset).take(pageSize)
    val totalRows = count
    val result = dbConfig.db.run(query.result)
    result flatMap (items => totalRows map (rows => Page(items, page, offset, rows, pageSize)))
  }

  def findByAlias(alias:String) : Future[Option[Blog]] =
    dbConfig.db.run(blogs.filter(_.alias === alias).result.headOption)

  def create(owner:Author, name:String,alias:String,description:String,image:Option[String],logo:Option[String],url:Option[String], disqus:Option[String], googleAnalytics:Option[String], useAvatarAsLogo:Option[Boolean], twitter:Option[String], showAds:Option[Boolean], adsCode:Option[String]) : Future[Int] =
    insert(Blog(IdGenerator.nextId(classOf[Blog]), name, alias, description, image, logo, url, useAvatarAsLogo, disqus, googleAnalytics, BlogStatus.CREATED.id, owner.id, twitter, showAds, adsCode))

  def update(blog:Blog, name:String,alias:String,description:String,image:Option[String],logo:Option[String],url:Option[String], disqus:Option[String], googleAnalytics:Option[String], useAvatarAsLogo:Option[Boolean], twitter:Option[String], showAds:Option[Boolean], adsCode:Option[String], status:Int) : Future[Int] =
    update (blog.copy(name=name, alias=alias,  useAvatarAsLogo=useAvatarAsLogo, description=description, image=image, logo=logo, url=url, disqus=disqus, googleAnalytics=googleAnalytics,  showAds=showAds, adsCode=adsCode, status=status))

}
