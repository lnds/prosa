package models

import java.sql.Timestamp

import org.joda.time.DateTime
import play.api.db.slick.Config.driver.simple._

case class Post(id:String, blog:String, image:Option[String], title:String, subtitle:Option[String], content:String, slug:Option[String], draft:Boolean, created:Option[Timestamp], published:Option[Timestamp], author:String) extends Identifiable {

  def publishedDate() = new DateTime(published.getOrElse(0))

}

class PostEntity(tag:Tag) extends Table[Post](tag, "post") {

  def id = column[String]("id", O.PrimaryKey, O.NotNull)
  def blog = column[String]("blog", O.Length(45, varying = true))
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





