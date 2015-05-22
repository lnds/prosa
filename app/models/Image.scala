package models

import play.api.db.slick.Config.driver.simple._

case class Image(id:String, filename:String, contentType:String, url:Option[String]) extends Identifiable

class ImageEntity(tag:Tag) extends Table[Image](tag, "image") {

  def id = column[String]("id", O.PrimaryKey)
  def filename = column[String]("filename")
  def contentType = column[String]("contenttype")
  def url = column[String]("url", O.Nullable)

  def * = (id,filename,contentType,url.?) <> (Image.tupled, Image.unapply)
}


