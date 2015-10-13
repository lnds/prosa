package services

import slick.driver.PostgresDriver.api._

import models.{Image}
import tools.IdGenerator


class ImageEntity(tag:Tag) extends Table[Image](tag, "image") with HasId {

  def id = column[String]("id", O.PrimaryKey)
  def filename = column[String]("filename")
  def contentType = column[String]("contenttype")
  def url = column[Option[String]]("url")

  def * = (id,filename,contentType,url) <> (Image.tupled, Image.unapply)
}

object ImageService extends DbService[Image] {

  type EntityType = ImageEntity
  val items = TableQuery[ImageEntity]
  lazy val images = items

  def addImage(filename:String, contentType:String)= {
    val image = Image(IdGenerator.nextId(classOf[Image]), filename, contentType, None)
    insert(image)
    image
  }

}