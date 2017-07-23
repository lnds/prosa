package models

case class Image(id:String, filename:String, contentType:String, url:Option[String]) extends Identifiable


