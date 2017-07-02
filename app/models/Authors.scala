package models

import javax.inject.{ Inject, Singleton }

import org.mindrot.jbcrypt.BCrypt
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._
import tools.{IdGenerator, PostAux}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

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





class Authors(tag:Tag) extends Table[Author](tag, "author") with HasId {

  def id = column[String]("id", O.PrimaryKey)
  def nickname = column[String]("nickname")
  def email = column[String]("email")
  def password = column[String]("password")
  def permission = column[String]("permission")
  def fullname = column[Option[String]]("fullname")
  def bio = column[Option[String]]("bio")
  def twitter = column[Option[String]]("twitter_handle")

  def * = (id,nickname,email,password,permission,fullname,bio, twitter) <> (Author.tupled, Author.unapply)

}

@Singleton
class AuthorsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends EntityService[Author] {

  type EntityType = Authors
  val items: TableQuery[Authors] = TableQuery[Authors]
  lazy val authors: PostgresDriver.api.TableQuery[Authors] = items

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

  def create(nickname:String, email:String, password:String, permission:String, twitter:Option[String]) : Future[Option[Author]]= {
    val pass = BCrypt.hashpw(password, BCrypt.gensalt())
    val author = Author(IdGenerator.nextId(classOf[Author]), nickname, email, pass, permission, None, None, twitter)
    insert(author).map { i => Some(author) }
  }

  def changePassword(author:Author, newPassword:String) : Future[Int] = {
    update(author.copy(password = BCrypt.hashpw(newPassword, BCrypt.gensalt())))
  }

  def avatar(authorId:String) : Future[Option[String]] =
    findById(authorId).map {
      case Some(a) => PostAux.avatarUrl(a.email)
      case None => None
    }

  override def queryFilter(qry: String, c: Authors) = c.nickname like "%" + qry + "%"

  override def queryOrder(c: EntityType) = c.nickname.asc
}
