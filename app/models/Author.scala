package models

import play.api.db.slick.Config.driver.simple._
import org.mindrot.jbcrypt.BCrypt
import services.EntityService
import tools.IdGenerator


sealed trait Visitor

case object Guest extends Visitor

case class Author(
  id:String,
  nickname:String,
  email:String,
  password:String,
  permission: String,
  fullname:Option[String],
  bio:Option[String]
) extends Visitor with Identifiable

class AuthorEntity(tag:Tag) extends Table[Author](tag, "author") with HasId {

  def id = column[String]("id", O.PrimaryKey)
  def nickname = column[String]("nickname")
  def email = column[String]("email")
  def password = column[String]("password")
  def permission = column[String]("permission")
  def fullname = column[String]("fullname", O.Nullable)
  def bio = column[String]("bio", O.Nullable)

  def * = (id,nickname,email,password,permission,fullname.?,bio.?) <> (Author.tupled, Author.unapply)

}


