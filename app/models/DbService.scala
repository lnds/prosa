package models

import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._
import slick.lifted.ColumnOrdered
import tools.IdGenerator

import scala.concurrent.Future

trait HasId {

  def id: Rep[String]
}


trait HasOwner extends HasId {
  def owner: Rep[String]
}


trait Identifiable {

  val id: String

}

trait DAOService[Entity <: Identifiable, I] {

  def insert(item: Entity): Future[Int]
  def update(item: Entity): Future[Int]
  def delete(id: I): Future[Int]
  def findById(id: I): Future[Option[Entity]]
  def count: Future[Int]
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page[Entity]]

}


trait DbService[Entity <: Identifiable] extends DAOService[Entity,String] {

  type EntityType <: Table[Entity]
  type TableType = TableQuery[EntityType]

  val items: TableType

  def genId(c: Class[Entity]) = IdGenerator.nextId(c)

  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  override def count : Future[Int] = dbConfig.db.run(items.length.result)

  private def filterQuery(id:String) : Query[EntityType, Entity, Seq] = items.filter(i => i.asInstanceOf[HasId].id === id)


  def findById(id: String) : Future[Option[Entity]]= dbConfig.db.run(filterQuery(id).result.headOption)

  override def insert(item: Entity) : Future[Int] = dbConfig.db.run(items += item)

  override def update(item: Entity) : Future[Int] = dbConfig.db.run(filterQuery(item.id).update(item))

  override def delete(id:String) : Future[Int] = dbConfig.db.run(filterQuery(id).delete)

  override def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page[Entity]] = {
    val offset = pageSize * page
    val query = (for {item <- items} yield item).drop(offset).take(pageSize)
    val totalRows = count
    val result = dbConfig.db.run(query.result)
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
