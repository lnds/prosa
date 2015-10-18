package models

import slick.driver.PostgresDriver.api._
import tools.IdGenerator

case class Image(id:String, filename:String, contentType:String, url:Option[String]) extends Identifiable


class Images(tag:Tag) extends Table[Image](tag, "image") with HasId {

  def id = column[String]("id", O.PrimaryKey)
  def filename = column[String]("filename")
  def contentType = column[String]("contenttype")
  def url = column[Option[String]]("url")

  def * = (id,filename,contentType,url) <> (Image.tupled, Image.unapply)
}

object Images extends DbService[Image] {

  type EntityType = Images
  val items = TableQuery[Images]
  lazy val images = items

  def addImage(filename:String, contentType:String)= {
    val image = Image(IdGenerator.nextId(classOf[Image]), filename, contentType, None)
    insert(image)
    image
  }

}