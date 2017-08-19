package dal

import models.{Identifiable, Page}
import play.api.db.slick.HasDatabaseConfigProvider
import scala.concurrent.{ExecutionContext, Future}
import slick.driver.JdbcProfile
import tools.IdGenerator


/**
  * Created by ediaz on 7/23/17.
  */


abstract class DbService[E <: Identifiable](implicit ec:ExecutionContext)
  extends DAOService[E,String] with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  type EntityType <: Table[E]
  type TableType = TableQuery[EntityType]

  val items: TableType

  def genId(c: Class[E]): String = IdGenerator.nextId(c)

  override def count : Future[Int] = db.run(items.length.result)

  private[this] def filterQuery(id:String) : Query[EntityType, E, Seq] = items.filter(i => i.asInstanceOf[HasId].id === id)

  def findById(id: String) : Future[Option[E]]= db.run(filterQuery(id).result.headOption)

  override def insert(item: E) : Future[Int] = db.run(items += item)

  override def update(item: E) : Future[Int] = db.run(filterQuery(item.id).update(item))

  override def delete(id:String) : Future[Int] = db.run(filterQuery(id).delete)

  override def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page[E]] = {
    val offset = pageSize * page
    val query = (for {item <- items} yield item).drop(offset).take(pageSize)
    val totalRows = count
    val result = db.run(query.result)
    result flatMap (items => totalRows map (rows => Page(items, page, offset, rows, pageSize)))
  }

}

