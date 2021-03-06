package tools

import java.nio.file.{Files, Paths}

import awscala._
import awscala.s3.{Bucket, S3}
import com.amazonaws.services.s3.model.ObjectMetadata
import controllers.routes
import javax.inject.{Inject, Singleton}

import play.api.Configuration

@Singleton
class ContentManager @Inject()(configuration:Configuration) {


  val cdnurl: Option[String] = configuration.getString("prosa.cdn.url")


  def putFile(key:String, file:File, contentType:String): String =
    cdnurl match {
      case None => routes.ContentController.image(key).url
      case Some(url) =>
        if (url.isEmpty)
          routes.ContentController.image(key).url
        else
          AmazonS3.putFile(url, key, file, contentType)
    }


}


private object AmazonS3 {

  lazy implicit val s3 = S3.at(Region.US_EAST_1)

  def putFile(cdnurl: String, key: String, file: File, contentType: String): String = {
    val bucket: Bucket = s3.bucket("prosa-bucket").getOrElse(s3.createBucket("prosa-bucket"))
    val byteArray = Files.readAllBytes(Paths.get(file.getAbsolutePath))
    val metadata: ObjectMetadata = new ObjectMetadata()
    metadata.setContentType(contentType)
    metadata.setContentLength(byteArray.length)
    bucket.putObjectAsPublicRead(key, byteArray, metadata)
    cdnurl + key
  }

}
