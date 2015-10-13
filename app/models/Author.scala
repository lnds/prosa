package models


sealed trait Visitor

case object Guest extends Visitor

case class Author(
  id:String,
  nickname:String,
  email:String,
  password:String,
  permission: String,
  fullname:Option[String],
  bio:Option[String]
) extends Visitor with Identifiable



