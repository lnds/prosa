package controllers

import jp.t2v.lab.play2.auth.{AuthElement, OptionalAuthElement}
import models._
import play.api.mvc.Controller


object BlogsController extends Controller with TokenValidateElement with AuthElement with AuthConfigImpl {

  def create = TODO

  def createBlog = TODO

}
