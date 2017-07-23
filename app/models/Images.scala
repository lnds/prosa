package models

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.PostgresDriver

import scala.concurrent.ExecutionContext
import slick.driver.PostgresDriver.api._
import slick.lifted.ProvenShape
import tools.IdGenerator

case class Image(id:String, filename:String, contentType:String, url:Option[String]) extends Identifiable


class Images(tag:Tag) extends Table[Image](tag, "image") with HasId {

  def id: Rep[String] = column[String]("id", O.PrimaryKey)
  def filename: Rep[String] = column[String]("filename")
  def contentType: Rep[String] = column[String]("contenttype")
  def url: Rep[Option[String]] = column[Option[String]]("url")

  def * : ProvenShape[Image] = (id,filename,contentType,url) <> (Image.tupled, Image.unapply)
}

@Singleton
class ImagesDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends DbService[Image] {

  type EntityType = Images
  val items: TableQuery[Images] = TableQuery[Images]
  lazy val images: PostgresDriver.api.TableQuery[Images] = items

  def addImage(filename:String, contentType:String): Image = {
    val image = Image(IdGenerator.nextId(classOf[Image]), filename, contentType, None)
    insert(image)
    image
  }

}