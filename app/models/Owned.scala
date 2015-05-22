package models

import play.api.db.slick.Config.driver.simple._
import services.AuthorService

trait Owned extends Identifiable {

   val owner: String

   def author(implicit s: Session) = AuthorService.findById(owner).get

 }
