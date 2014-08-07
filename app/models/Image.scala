package models

import play.api.db.slick.Config.driver.simple._

case class Image(id:String, filename:String, contentType:String, url:Option[String])

class Images(tag:Tag) extends Table[Image](tag, "image") {
  def id = column[String]("id", O.PrimaryKey)
  def filename = column[String]("filename")
  def contentType = column[String]("contentType")
  def url = column[String]("url", O.Nullable)

  def * = (id,filename,contentType,url.?) <> (Image.tupled, Image.unapply)
}

object Images {

  val images = TableQuery[Images]

  def findById(id:String)(implicit s:Session) : Option[Image] = {
    images.filter(_.id===id).firstOption
  }

  def addImage(filename:String, contentType:String)(implicit s:Session) = {
    val image = Image(IdGenerator.nextId(classOf[Image]), filename, contentType, None)
    insert(image)
    image
  }

  def insert(image:Image)(implicit s:Session) {
    images.insert(image)
  }

  def update(image:Image)(implicit s:Session) {
    images.filter(_.id === image.id).update(image)
  }
}
