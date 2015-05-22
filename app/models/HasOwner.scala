package models

import play.api.db.slick.Config.driver.simple._

trait HasOwner extends HasId {
   def owner: Column[String]
 }
