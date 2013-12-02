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
      "id" -> JsNumber(BigDecimal(publisher.id)),
      "name" -> JsString(publisher.name),
      "active" -> JsString(if (publisher.active) "true" else "false"),
      "url" -> JsString(publisher.url)))
  }

  implicit object CreativeFormat extends Format[Creative] {
    def reads(json: JsValue) = JsSuccess(new Creative(
      (json \ "name").as[String],
      (json \ "url").as[Option[String]]))

    def writes(creative: Creative) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(creative.id)),
      "name" -> JsString(creative.name),
      "preview" -> JsString(creative.getPreview()),
      "uuid" -> JsString(creative.uuid),
      "url" -> JsString(creative.url)))
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

  def uploadCreative = (publisherid: String) => IsAuthenticated { adminid =>
    request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Publisher.findById(publisherid, admin).map { publisher =>
          request.body.asMultipartFormData.map { body =>
            body.file("creative").map { file =>
              Creative.addUpload(publisher, file.contentType.getOrElse("application/octet-steam"),
                file.filename, file.ref.file).map { creative =>
                  Ok(Json.toJson(creative))
                }.getOrElse(NotFound)
            }.getOrElse(NotFound)
          }.getOrElse(NotFound)
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def publishers = IsAuthenticated { adminid =>
    _ =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(
          html.publishers(
            Publisher.findByAdmin(admin).asScala,
            admin))
      }.getOrElse(Forbidden)
  }

  def publisherJson(admin: Admin): JsValue =
    Json.toJson(Publisher.findByAdmin(admin).asScala)

  /** Action to get the publishers */
  def publisherList(page: Int, perPage: Int) = IsAuthenticated { adminid =>
    _ =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(publisherJson(admin))
      }.getOrElse(Forbidden)
  }

  def message = IsAuthenticated { adminid =>
    _ =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(publisherJson(admin))
      }.getOrElse(Forbidden)
  }

  def dashboard = IsAuthenticated { adminid =>
    _ =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(Json.toJson(
          Publisher.statsByAdmin(admin).asScala))
      }.getOrElse(Forbidden)
  }

  def stats(publisherid: Long) = Action(parse.json) { implicit req =>
    Ok(Json.toJson(""))
  }
}