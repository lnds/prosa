package dal

import models.{Identifiable, Page}

import scala.concurrent.Future

/**
  * Created by ediaz on 7/23/17.
  */

trait DAOService[E <: Identifiable, I] {

  def insert(item: E): Future[Int]
  def update(item: E): Future[Int]
  def delete(id: I): Future[Int]
  def findById(id: I): Future[Option[E]]
  def count: Future[Int]
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page[E]]

}
