package controllers

import java.io.File
import javax.inject.Inject

import jp.t2v.lab.play2.auth.AuthElement
import jp.t2v.lab.play2.stackc.StackableController
import models.{AuthorsDAO, Images, ImagesDAO, Writer}
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.mvc.Controller
import tools.ContentManager

import scala.concurrent.ExecutionContext.Implicits.global


class ImagesController @Inject()(val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider, private val contentManager: ContentManager, private val imagesDAO: ImagesDAO, protected val  authorsDAO:AuthorsDAO)
  extends Controller with AuthElement with AuthConfigImpl with I18nSupport {

  val createForm = Form(
    tuple(
      "filename" -> nonEmptyText,
      "contentType" -> nonEmptyText
    )
  )

  def upload = StackAction(parse.multipartFormData, AuthorityKey -> Writer) { implicit request =>
    val tempFile = File.createTempFile("image_", ".img")
    val image =
      request.body.file("file").map { file =>
        val contentType = file.contentType.getOrElse("")
        file.ref.moveTo(tempFile, replace = true)
        Map("filename" -> tempFile.getAbsolutePath, "contentType" -> contentType)
      }
    createForm.bind(image.get).fold(
      formWithErrors => NotFound,
      imageData => {
        val img = imagesDAO.addImage(imageData._1, imageData._2)
        val url = contentManager.putFile(img.id, tempFile, img.contentType)
        imagesDAO.update(img.copy(url = Some(url)))
        Ok(url)
      }
    )
  }

  case class ImageFile(url: String)

  def editorUpload = StackAction(parse.multipartFormData, AuthorityKey -> Writer) { implicit request =>
    request.body.files.headOption.map { image =>
      val tempFile = File.createTempFile("image_", ".img")
      image.ref.moveTo(tempFile, replace = true)
      val img = imagesDAO.addImage(tempFile.getAbsolutePath, image.contentType.getOrElse(""))
      val url = contentManager.putFile(img.id, tempFile, img.contentType)
      imagesDAO.update(img.copy(url = Some(url)))
      Ok(Json.obj("files" -> Json.arr(Json.obj("url" -> url))))
    } getOrElse NotFound
  }


}

class ContentController @Inject()(val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider, private val imagesDAO: ImagesDAO)
extends Controller with StackableController {

  /**
   * This method get temporal file, you should configure a CDN in application.conf
   */
  def image(id: String) = AsyncStack {
    implicit request =>
      imagesDAO.findById(id).map {
        case Some(img) =>
          val source = scala.io.Source.fromFile(img.filename)(scala.io.Codec.ISO8859)
          val byteArray = source.map(_.toByte).toArray
          source.close()
          Ok(byteArray).as(img.contentType)
        case None =>
          NotFound
      }
  }

}
