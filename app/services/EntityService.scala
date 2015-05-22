package services

import models.{Identifiable, Page}
import play.api.db.slick.Config.driver.simple._

import scala.slick.lifted.ColumnOrdered

trait EntityService[T <: Identifiable] extends DbService[T] {

  def queryFilter(qry: String, c: EntityType): Column[Boolean]

  def queryOrder(c: EntityType): ColumnOrdered[String]

  def countQuery(qryStr: String)(implicit s: Session): Int = {
    Query(items.filter(c => queryFilter(qryStr, c)).length).first
  }

  def pageQuery(page: Int, offset: Int, pageSize: Int, filter: Option[String]): Query[EntityType, T, Seq] = {
    (if (filter.isEmpty) for {i <- items} yield i
    else for {i <- items if queryFilter(filter.get, i)} yield i)
      .sortBy(queryOrder)
      .drop(offset)
      .take(pageSize)
  }

  def list(page: Int = 0, pageSize: Int = 10)(implicit s: Session): Page[T] = {
    val offset = pageSize * page
    val query = pageQuery(page, offset, pageSize, None)
    val totalRows = count()
    val result = query.list.map(row => row)
    Page(result, page, offset, totalRows, pageSize)
  }

  def search(queryStr: String, page: Int = 0, pageSize: Int = 50)(implicit s: Session): Page[T] = {
    val offset = pageSize * page
    val query = pageQuery(page, offset, pageSize, Some(queryStr))
    val totalRows = countQuery(queryStr)
    val result = query.list.map(row => row)
    Page(result, page, offset, totalRows, pageSize)
  }

}
