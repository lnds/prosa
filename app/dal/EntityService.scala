package dal

import models.{Identifiable, Page}
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._
import slick.lifted.ColumnOrdered
import tools.IdGenerator

/**
  * Created by ediaz on 7/23/17.
  */

trait EntityService[T <: Identifiable] extends DbService[T] {

  def queryFilter(qry: String, c: EntityType): Rep[Boolean]

  def queryOrder(c: EntityType): ColumnOrdered[String]

  def countQuery(qryStr: String): Future[Int] =
    dbConfig.db.run(items.filter(c => queryFilter(qryStr, c)).length.result)

  def pageQuery(page: Int, offset: Int, pageSize: Int, filter: Option[String]): Query[EntityType, T, Seq] = {
    (if (filter.isEmpty) for {i <- items} yield i
    else for {i <- items if queryFilter(filter.get, i)} yield i)
      .sortBy(queryOrder)
      .drop(offset)
      .take(pageSize)
  }


  def search(queryStr: String, page: Int = 0, pageSize: Int = 50)(implicit s: Session): Future[Page[T]] = {
    val offset = pageSize * page
    val query = pageQuery(page, offset, pageSize, Some(queryStr))
    val totalRows = countQuery(queryStr)
    val result = dbConfig.db.run(query.result)
    result flatMap (items => totalRows map (rows => Page(items, page, offset, rows, pageSize)))
  }

}
