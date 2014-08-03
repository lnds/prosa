package controllers

import jp.t2v.lab.play2.auth.AuthElement
import play.api.mvc.Controller

object AuthorsController extends Controller  with TokenValidateElement with AuthElement with AuthConfigImpl {

  def changePassword = TODO

  def savePassword = TODO

}
