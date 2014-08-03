package controllers

import jp.t2v.lab.play2.auth.OptionalAuthElement
import play.api.mvc.Controller

object PostsGuestController extends Controller with DBElement with OptionalAuthElement with AuthConfigImpl  {

  def index(alias:String, pageNum:Int=0) = TODO

  def viewPost(alias:String, year:Int, month:Int, day:Int, slug:String) = TODO

  def atom(alias:String) = TODO

}
