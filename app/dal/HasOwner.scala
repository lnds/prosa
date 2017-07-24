package dal

import slick.lifted.Rep

/**
  * Has an owner
  * Created by ediaz on 7/23/17.
  */

trait HasOwner extends HasId {
  def owner: Rep[String]
}
