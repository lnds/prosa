package services

import models.{Author, AuthorEntity}
import org.mindrot.jbcrypt.BCrypt
import play.api.db.slick.Config.driver.simple._
import tools.IdGenerator

/**
 * AuthorService
 * Created by ediaz on 21-05-15.
 */
object AuthorService extends EntityService[Author]  {

  type EntityType = AuthorEntity
  val items = TableQuery[AuthorEntity]
  lazy val authors = items

  def findByNickname(nickname: String)(implicit  s:Session) = authors.filter(_.nickname === nickname).firstOption

  def create(nickname:String, email:String, password:String, permission:String)(implicit  s:Session)  = {
    val pass = BCrypt.hashpw(password, BCrypt.gensalt())
    insert(Author(IdGenerator.nextId(classOf[Author]), nickname, email, pass, permission, None, None))
  }

  def changePassword(author:Author, newPassword:String)(implicit s:Session) {
    update(author.copy(password = BCrypt.hashpw(newPassword, BCrypt.gensalt())))
  }

  def queryFilter(qry: String, c: AuthorEntity) = c.nickname like "%" + qry + "%"

  def queryOrder(c: EntityType) = c.nickname.asc
}
