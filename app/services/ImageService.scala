package services

import models.{Image, ImageEntity}
import play.api.db.slick.Config.driver.simple._
import tools.IdGenerator

object ImageService extends DbService[Image] {

  type EntityType = ImageEntity
  val items = TableQuery[ImageEntity]
  lazy val images = items

  def addImage(filename:String, contentType:String)(implicit s:Session) = {
    val image = Image(IdGenerator.nextId(classOf[Image]), filename, contentType, None)
    insert(image)
    image
  }

}