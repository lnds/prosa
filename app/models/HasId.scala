package models

import play.api.db.slick.Config.driver.simple._

/**
 * HasId trait used in DbService
 * Created by ediaz on 21-05-15.
 */
trait HasId {

   def id: Column[String]
 }
