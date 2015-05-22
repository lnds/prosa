package models

import play.api.db.slick.Config.driver.simple._

trait HasId {

   def id: Column[String]
 }
