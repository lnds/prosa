package models


import org.joda.time.LocalDateTime

case class Post(id:String,
                blog:String,
                image:Option[String],
                title:String,
                subtitle:Option[String],
                content:String,
                slug:Option[String],
                draft:Boolean,
                created:Option[LocalDateTime],
                published:Option[LocalDateTime],
                author:String) extends Identifiable


