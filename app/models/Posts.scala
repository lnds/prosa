package models


import org.joda.time.{DateTime, Period}

case class Post(id:String,
                blog:String,
                image:Option[String],
                title:String,
                subtitle:Option[String],
                content:String,
                slug:Option[String],
                draft:Boolean,
                created:Option[DateTime],
                published:Option[DateTime],
                author:String) extends Identifiable


