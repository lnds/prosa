package tools

import awscala._
import awscala.s3.{Bucket, S3}
import com.amazonaws.services.s3.model.ObjectMetadata
import controllers.routes
import play.api.{Logger, Play}
import play.api.Play.current


object ContentManager {


  val cdnurl = Play.application.configuration.getString("prosa.cdn.url")


  def putFile(key:String, file:File, contentType:String) =
    cdnurl match {
      case None => routes.ContentController.getImage(key).url
      case Some(url) =>
        if (url.isEmpty)
          routes.ContentController.getImage(key).url
        else
          AmazonS3.putFile(url, key, file, contentType)
    }


}


private object AmazonS3 {

  lazy implicit val s3 = S3.at(Region.US_EAST_1)

  def putFile(cdnurl: String, key: String, file: File, contentType: String) = {
    Logger.info("@ putFile")
    val bucket: Bucket = s3.bucket("prosa-bucket").getOrElse(s3.createBucket("prosa-bucket"))
    Logger.info("Bucket = "+bucket)
    val source = scala.io.Source.fromFile(file)(scala.io.Codec.ISO8859)
    val byteArray = source.map(_.toByte).toArray
    val metadata: ObjectMetadata = new ObjectMetadata()
    metadata.setContentType(contentType)
    metadata.setContentLength(byteArray.length)
    bucket.putObjectAsPublicRead(key, byteArray, metadata)
    cdnurl + key
  }

}
