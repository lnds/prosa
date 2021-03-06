package controllers

import dal.{AuthorsDAO, ImagesDAO}
import java.io.File
import java.nio.file.{Files, Paths}
import javax.inject.Inject

import jp.t2v.lab.play2.auth.AuthElement
import jp.t2v.lab.play2.stackc.StackableController
import models.Writer
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, Controller}
import tools.ContentManager

import scala.concurrent.ExecutionContext



class ImagesController @Inject()(val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider, private val contentManager: ContentManager,
                                 private val imagesDAO: ImagesDAO, val  authorsDAO:AuthorsDAO, implicit val ec:ExecutionContext)
  extends Controller with AuthElement with AuthConfigImpl with I18nSupport {

  val createForm = Form(
    tuple(
      "filename" -> nonEmptyText,
      "contentType" -> nonEmptyText
    )
  )

  def upload = StackAction(parse.multipartFormData, AuthorityKey -> Writer) { implicit request =>
    val tempFile = File.createTempFile("image_", ".img")
    val image = request.body.file("file").map { file =>
        val contentType = file.contentType.getOrElse("")
        file.ref.moveTo(tempFile, replace = true)
        Map("filename" -> tempFile.getAbsolutePath, "contentType" -> contentType)
      }
    createForm.bind(image.get).fold(
      formWithErrors => NotFound,
      imageData => {
        val (filename, contentType) = imageData
        val img = imagesDAO.addImage(filename, contentType)
        val url = contentManager.putFile(img.id, tempFile, img.contentType)
        imagesDAO.update(img.copy(url = Some(url)))
        Ok(url)
      }
    )
  }

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

class ContentController @Inject()(val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider, private val imagesDAO: ImagesDAO,
                                 implicit val ec:ExecutionContext)
extends Controller with StackableController {

  /**
   * This method get temporal file, you should configure a CDN in application.conf
   */
  def image(id: String): Action[AnyContent] = AsyncStack {
    implicit request =>
      imagesDAO.findById(id).map {
        case Some(img) =>
          val byteArray = Files.readAllBytes(Paths.get(img.filename))
          Ok(byteArray).as(img.contentType)
        case None =>
          NotFound
      }
  }

}
