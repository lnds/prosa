package dal

import javax.inject.{Inject, Singleton}

import models.Image
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.ExecutionContext
import slick.driver.PostgresDriver
import slick.lifted.ProvenShape
import tools.IdGenerator


/**
  * Data Access Layer for Images
  * Created by ediaz on 7/23/17.
  */

@Singleton
class ImagesDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends DbService[Image] {

  import driver.api._

  class Images(tag:Tag) extends Table[Image](tag, "image") with HasId {

    def id: Rep[String] = column[String]("id", O.PrimaryKey, O.Length(keySize))
    def filename: Rep[String] = column[String]("filename")
    def contentType: Rep[String] = column[String]("contenttype")
    def url: Rep[Option[String]] = column[Option[String]]("url")

    def * : ProvenShape[Image] = (id,filename,contentType,url) <> (Image.tupled, Image.unapply)
  }


  type EntityType = Images
  val items: TableQuery[Images] = TableQuery[Images]
  lazy val images: PostgresDriver.api.TableQuery[Images] = items

  def addImage(filename:String, contentType:String): Image = {
    val image = Image(IdGenerator.nextId(classOf[Image]), filename, contentType, None)
    insert(image)
    image
  }

}