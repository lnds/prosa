package models

import javax.inject.{Inject, Singleton}

import org.mindrot.jbcrypt.BCrypt
import play.api.db.slick.DatabaseConfigProvider
import tools.{IdGenerator, PostAux}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import slick.driver.PostgresDriver.api._
import slick.lifted.{ColumnOrdered, ProvenShape}


/**
 * AuthorService
 * Created by ediaz on 21-05-15.
 */


sealed trait Visitor

case object Guest extends Visitor

case class Author(
                   id:String,
                   nickname:String,
                   email:String,
                   password:String,
                   permission: String,
                   fullname:Option[String],
                   bio:Option[String],
                   twitter:Option[String]
                 ) extends Visitor with Identifiable


