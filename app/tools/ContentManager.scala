package tools

import awscala._
import awscala.s3.{Bucket, S3}
import com.amazonaws.services.s3.model.ObjectMetadata
import controllers.routes
import play.api.Play
import play.api.Play.current


object ContentManager {


  val cdnurl = Play.application.configuration.getString("prosa.cdn.url").orNull


  def putFile(key:String, file:File, contentType:String) = {
    if (cdnurl == null || cdnurl.isEmpty)
      routes.ContentController.getImage(key).url
    else
      // patch
      AmazonS3.putFile(cdnurl, key, file, contentType)
  }

}


private object AmazonS3 {

  lazy implicit val s3 = S3()

  def putFile(cdnurl: String, key: String, file: File, contentType: String) = {
    val bucket: Bucket = s3.bucket("prosa-bucket").getOrElse(s3.createBucket("prosa-bucket"))
    val source = scala.io.Source.fromFile(file)(scala.io.Codec.ISO8859)
    val byteArray = source.map(_.toByte).toArray
    val metadata: ObjectMetadata = new ObjectMetadata()
    metadata.setContentType(contentType)
    metadata.setContentLength(byteArray.length)
    bucket.putObjectAsPublicRead(key, byteArray, metadata)
    cdnurl + key
  }

}
