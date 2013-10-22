package controllers

import play.api.libs.json._
import play.api.mvc.Action
import play.api.mvc.Controller
import scala.collection.JavaConverters._

import models._
import views._

object AdminController extends Controller with Secured {
  implicit object PublisherFormat extends Format[Publisher] {
    def reads(json: JsValue) = JsSuccess(new Publisher(
      (json \ "name").as[String],
      (json \ "url").as[Option[String]]))
      
    def writes(publisher: Publisher) = JsObject(Seq(
      "name" -> JsString(publisher.name),
      "url" -> Json.toJson(publisher.url)))
  }

  /** Action to get the publishers */
  def getPublishers(page: Int, perPage: Int) = IsAuthenticated { adminid =>
    implicit req =>
      Ok(Json.toJson(Publisher.findByAdmin(adminid).asScala))
  }

  /** Action to get admin */
  def getAdmin = Action(parse.json) { implicit req =>
    Ok(Json.toJson(""))
  }

  /** Action to save a admin */
  def saveAdmin = Action(parse.json) { implicit req =>
    Ok(Json.toJson(""))
  }
}