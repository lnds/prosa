package dal

import slick.lifted.Rep


/**
  * Has an Id
  * Created by ediaz on 7/23/17.
  */
trait HasId {

  def id: Rep[String]
}
