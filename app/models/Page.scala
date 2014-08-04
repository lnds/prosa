package models

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long, pageSize: Int) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)

  def countPages() = {
    val count = total / pageSize
    val offset = total - (count*pageSize)
    if (offset > 0)
      count + 1
    else
      count
  }
}
