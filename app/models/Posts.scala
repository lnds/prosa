package models

import com.github.tototoshi.slick.PostgresJodaSupport._
import javax.inject.{Inject, Singleton}

import org.joda.time.{DateTime, Period}
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import slick.driver.PostgresDriver.api._
import slick.lifted.ProvenShape
import tools.IdGenerator



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


