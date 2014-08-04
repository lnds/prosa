package tools

import awscala._
import awscala.s3.{Bucket, S3}
import com.amazonaws.services.s3.model.ObjectMetadata
import play.api.Play
import play.api.Play.current


object ContentManager {

  implicit val s3 = S3()


  val bucket: Bucket = s3.bucket("prosa-bucket").getOrElse(s3.createBucket("prosa-bucket"))

  def putFile(key:String, file:File, contentType:String) = {
    val source = scala.io.Source.fromFile(file)(scala.io.Codec.ISO8859)
    val byteArray = source.map(_.toByte).toArray
    val metadata : ObjectMetadata = new ObjectMetadata()
    metadata.setContentType(contentType)
    metadata.setContentLength(byteArray.length)
    bucket.putObjectAsPublicRead(key, byteArray, metadata)
    val result =  Play.application.configuration.getString("prosa.cdn.url").getOrElse("")+key
    result
  }
}
