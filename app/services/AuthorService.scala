package services

import models.Author
import org.mindrot.jbcrypt.BCrypt
import slick.driver.PostgresDriver.api._
import tools.{PostAux, IdGenerator}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * AuthorService
 * Created by ediaz on 21-05-15.
 */


class AuthorEntity(tag:Tag) extends Table[Author](tag, "author") with HasId {

  def id = column[String]("id", O.PrimaryKey)
  def nickname = column[String]("nickname")
  def email = column[String]("email")
  def password = column[String]("password")
  def permission = column[String]("permission")
  def fullname = column[Option[String]]("fullname")
  def bio = column[Option[String]]("bio")

  def * = (id,nickname,email,password,permission,fullname,bio) <> (Author.tupled, Author.unapply)

}

object AuthorService extends EntityService[Author]  {

  type EntityType = AuthorEntity
  val items = TableQuery[AuthorEntity]
  lazy val authors = items

  def authenticate(username: String, password:String) =
    findByNickname(username).map {
      case None => None
      case Some(a) =>
        if (BCrypt.checkpw(password, a.password))
          Some(a)
        else
          None
    }


  def findByNickname(nickname: String) : Future[Option[Author]] =
    dbConfig.db.run(authors.filter(a => a.nickname === nickname).result.headOption)

  def create(nickname:String, email:String, password:String, permission:String)(implicit  s:Session)  = {
    val pass = BCrypt.hashpw(password, BCrypt.gensalt())
    insert(Author(IdGenerator.nextId(classOf[Author]), nickname, email, pass, permission, None, None))
  }

  def changePassword(author:Author, newPassword:String) {
    update(author.copy(password = BCrypt.hashpw(newPassword, BCrypt.gensalt())))
  }

  def getAvatar(authorId:String)  =
    findById(authorId).map {
      case Some(a) => PostAux.avatarUrl(a.email)
      case None => null
    }

  override def queryFilter(qry: String, c: AuthorEntity) = c.nickname like "%" + qry + "%"

  override def queryOrder(c: EntityType) = c.nickname.asc
}
