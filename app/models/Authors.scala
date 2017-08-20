package models


/**
 * AuthorService
 * Created by ediaz on 21-05-15.
 */


sealed trait Visitor

case object Guest extends Visitor

case class Author(
                   id:String,
                   nickname:String,
                   email:String,
                   password:String,
                   permission: String,
                   fullname:Option[String],
                   bio:Option[String],
                   twitter:Option[String]
                 ) extends Visitor with Identifiable


