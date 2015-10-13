package models

import java.sql.Timestamp
import org.joda.time.DateTime

case class Post(id:String, blog:String, image:Option[String], title:String, subtitle:Option[String], content:String, slug:Option[String], draft:Boolean, created:Option[Timestamp], published:Option[Timestamp], author:String) extends Identifiable {

  def publishedDate() = new DateTime(published.getOrElse(0))

}






