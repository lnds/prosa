package services

import models.{HasId, Identifiable}
import play.api.db.slick.Config.driver.simple._
import tools.IdGenerator


trait DbService[C <: Identifiable] {

  type EntityType <: Table[C]
  type TableType = TableQuery[EntityType]

  val items: TableType

  def genId(c: Class[C]) = IdGenerator.nextId(c)

  def findById(id: String)(implicit s: Session) = {
    items.filter(e => e.asInstanceOf[HasId].id === id).firstOption
  }

  def count()(implicit s: Session): Int = Query(items.length).first

  def insert(item: EntityType#TableElementType)(implicit s: Session) {
    items.insert(item)

  }

  def update(item: EntityType#TableElementType)(implicit s: Session) {
    items.filter(e => e.asInstanceOf[HasId].id === item.asInstanceOf[Identifiable].id).update(item)
  }


  def delete(item: EntityType#TableElementType)(implicit s: Session) {
    items.filter(e => e.asInstanceOf[HasId].id === item.asInstanceOf[Identifiable].id).delete
  }

  def delete(id: String)(implicit s: Session) {
    items.filter(e => e.asInstanceOf[HasId].id === id).delete
  }

}