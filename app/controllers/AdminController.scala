package controllers

import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller

object AdminController extends Controller {

  /** Action to get the publishers */
  def getPublishers(page: Int, perPage: Int) = Action { implicit req =>
    Ok(Json.toJson(""))
  }

  /** Action to get admin */
  def getAdmin = Action(parse.json) { req =>
    Ok(Json.toJson(""))
  }

  /** Action to save a admin */
  def saveAdmin = Action(parse.json) { req =>
    Ok(Json.toJson(""))
  }
}