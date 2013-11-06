package controllers

import play.api.libs.json._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import scala.collection.JavaConverters._

import models._
import views._

object PublisherController extends Controller with Secured {
  implicit object PublisherFormat extends Format[Publisher] {
    def reads(json: JsValue) = JsSuccess(new Publisher(
      (json \ "name").as[String],
      (json \ "url").as[Option[String]]))

    def writes(publisher: Publisher) = JsObject(Seq(
      "name" -> JsString(publisher.name),
      "url" -> JsString(publisher.url)))
  }

  implicit object StringMapFormat extends Format[java.util.Map[String, String]] {
    def reads(json: JsValue) = JsSuccess(null)

    def writes(map: java.util.Map[String, String]) = JsObject(
      map.entrySet().asScala.toSeq.map(e =>
        e.getKey() -> JsString(e.getValue())))
  }

  implicit object DatasetFormat extends Format[Dataset] {
    def reads(json: JsValue) = JsSuccess(null)

    def writes(dataset: Dataset) = JsObject(Seq(
      "values" -> Json.toJson(dataset.getValues().asScala.toSeq),
      "type" -> JsString(dataset.getType()),
      "name" -> JsString(dataset.getName())))
  }

  def publisherJson(adminid: String) =
    Json.toJson(Publisher.findByAdmin(adminid).asScala)

  /** Action to get the publishers */
  def publishers(page: Int, perPage: Int) = IsAuthenticated { adminid =>
    _ => Option(adminid).map { id =>
          Ok(publisherJson(id))
      }.getOrElse(Forbidden)
  }

  def dashboard = IsAuthenticated { adminid =>
    _ => Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(Json.toJson(
          Publisher.statsByAdmin(admin).asScala))
      }.getOrElse(Forbidden)
  }

  def stats(publisherid: Long) = Action(parse.json) { implicit req =>
    Ok(Json.toJson(""))
  }
}