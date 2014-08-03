package models


sealed trait Permission
case object Administrator extends Permission
case object Editor extends Permission
case object Writer extends Permission

object Permission {

  def valueOf(value:String): Permission = value match {
    case "Administrator" => Administrator
    case "Editor" => Editor
    case "Writer" => Writer
    case _ => throw new IllegalArgumentException()
  }

}