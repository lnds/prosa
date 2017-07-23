package models

import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._
import slick.lifted.ColumnOrdered
import tools.IdGenerator

trait HasId {

  def id: Rep[String]
}


trait HasOwner extends HasId {
  def owner: Rep[String]
}


trait Identifiable {

  val id: String

}

trait DAOService[E <: Identifiable, I] {

  def insert(item: E): Future[Int]
  def update(item: E): Future[Int]
  def delete(id: I): Future[Int]
  def findById(id: I): Future[Option[E]]
  def count: Future[Int]
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page[E]]

}


trait DbService[E <: Identifiable]
  extends DAOService[E,String] with HasDatabaseConfigProvider[JdbcProfile] {

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
