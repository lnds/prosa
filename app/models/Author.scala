package models

import play.api.db.slick.Config.driver.simple._
import org.mindrot.jbcrypt.BCrypt


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
) extends Visitor

class Authors(tag:Tag) extends Table[Author](tag, "author") {
  def id = column[String]("id", O.PrimaryKey)
  def nickname = column[String]("nickname")
  def email = column[String]("email")
  def password = column[String]("password")
  def permission = column[String]("permission")
  def fullname = column[String]("fullname", O.Nullable)
  def bio = column[String]("bio", O.Nullable)

  def * = (id,nickname,email,password,permission,fullname.?,bio.?) <> (Author.tupled, Author.unapply _)

}

object Authors {

  val authors = TableQuery[Authors]

  def findById(id: String)(implicit  s:Session) : Option[Author] = authors.filter(_.id === id).firstOption

  def findByNickname(nickname: String)(implicit  s:Session) = authors.filter(_.nickname === nickname).firstOption

  def create(nickname:String, email:String, password:String, permission:String)(implicit  s:Session)  = {
    val pass = BCrypt.hashpw(password, BCrypt.gensalt())
    insert(Author(IdGenerator.nextId(classOf[Author]), nickname, email, pass, permission, None, None))
  }

  def insert(author:Author)(implicit s:Session) {
    authors.insert(author)
  }

  def update(author:Author)(implicit s:Session) {
    authors.filter(_.id===author.id).update(author)
  }

  def changePassword(author:Author, newPassword:String)(implicit s:Session) {
    update(author.copy(password = BCrypt.hashpw(newPassword, BCrypt.gensalt())))
  }

}
