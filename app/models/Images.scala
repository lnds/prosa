package models

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.PostgresDriver

import scala.concurrent.ExecutionContext
import slick.driver.PostgresDriver.api._
import slick.lifted.ProvenShape
import tools.IdGenerator

case class Image(id:String, filename:String, contentType:String, url:Option[String]) extends Identifiable


