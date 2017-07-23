package dal

import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.lifted.Rep
import tools.IdGenerator


/**
  * Has an Id
  * Created by ediaz on 7/23/17.
  */
trait HasId {

  def id: Rep[String]
}
