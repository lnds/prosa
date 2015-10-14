package controllers

import java.io.File
import javax.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import jp.t2v.lab.play2.stackc.StackableController
import models.{Images, Writer}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.libs.json._
import play.api.mvc.Controller
import tools.ContentManager
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ImagesController @Inject()(val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider) extends Controller with AuthElement with AuthConfigImpl with I18nSupport {

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
        Logger.info("imageData = " + imageData._1 + " , " + imageData._2)
        val img = Images.addImage(imageData._1, imageData._2)
        val url = ContentManager.putFile(img.id, tempFile, img.contentType)
        Images.update(img.copy(url = Some(url)))
        Logger.info("upload url = " + url)
        Ok(url)
      }
    )
  }

  case class ImageFile(url: String)

  def editorUpload = StackAction(parse.multipartFormData, AuthorityKey -> Writer) { implicit request =>
    val tempFile = File.createTempFile("image_", ".img")
    val image = request.body.files.head
    image.ref.moveTo(tempFile, replace = true)
    val img = Images.addImage(tempFile.getAbsolutePath, image.contentType.getOrElse(""))
    val url = ContentManager.putFile(img.id, tempFile, img.contentType)
    Images.update(img.copy(url = Some(url)))
    Logger.info("editor upload url = " + url)
    Ok(Json.obj("files" -> Json.arr(Json.obj("url" -> url))))
  }


}

class ContentController @Inject()(val messagesApi: MessagesApi, dbConfigProvider: DatabaseConfigProvider) extends Controller with StackableController {

  /**
   * This method get temporal file, you should configure a CDN in application.conf
   */
  def getImage(id: String) = AsyncStack {
    implicit request =>
      Images.findById(id).map {
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
