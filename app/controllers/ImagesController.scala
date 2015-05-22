package controllers

import java.io.File

import jp.t2v.lab.play2.auth.AuthElement
import models.Writer
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import play.api.mvc.Controller
import services.ImageService
import tools.ContentManager


object ImagesController extends Controller with DBElement with AuthElement with AuthConfigImpl  {

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
        file.ref.moveTo(tempFile, replace=true)
        Map("filename" -> tempFile.getAbsolutePath, "contentType" -> contentType)
      }
    createForm.bind(image.get).fold(
      formWithErrors => NotFound,
      imageData => {
        val img = ImageService.addImage(imageData._1, imageData._2)
        val url = ContentManager.putFile(img.id, tempFile, img.contentType)
        ImageService.update(img.copy(url=Some(url)))
        Ok(url)
      }
    )
  }

  case class ImageFile(url:String)

  def editorUpload = StackAction(parse.multipartFormData, AuthorityKey -> Writer) { implicit request =>
    val tempFile = File.createTempFile("image_", ".img")
    val image = request.body.files.head
    image.ref.moveTo(tempFile, replace=true)
    val img = ImageService.addImage(tempFile.getAbsolutePath, image.contentType.getOrElse(""))
    val url = ContentManager.putFile(img.id, tempFile, img.contentType)
    ImageService.update(img.copy(url = Some(url)))
    Ok(Json.obj("files" -> Json.arr(Json.obj("url" -> url))))
  }


}

object ContentController extends Controller with DBElement {

  /**
   * This method get temporal file, you should configure a CDN in application.conf
   */
  def getImage(id: String) = StackAction {
    implicit request =>
      ImageService.findById(id).map {
        img =>
          val source = scala.io.Source.fromFile(img.filename)(scala.io.Codec.ISO8859)
          val byteArray = source.map(_.toByte).toArray
          source.close()
          Ok(byteArray).as(img.contentType)
      } getOrElse NotFound
  }

}
