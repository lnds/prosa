package controllers

import jp.t2v.lab.play2.auth.{AuthElement, OptionalAuthElement}
import play.api.mvc.Controller


object PostsController extends Controller with TokenValidateElement with AuthElement with AuthConfigImpl  {

  def drafts(alias:String, pageNum:Int=0) = TODO

  def newPost(alias:String) = TODO

  def savePost(alias:String) = TODO

  def deletePost(alias:String, id:String) = TODO

  def unpublishPost(alias:String, id:String) = TODO

  def updatePost(alias:String, id:String) = TODO

  def editPost(alias:String, id:String) = TODO

}