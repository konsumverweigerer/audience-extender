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

object AudienceController extends Controller with Secured {
  implicit object AudienceFormat extends Format[Audience] {
    def reads(json: JsValue) = JsSuccess(new Audience(
      (json \ "name").as[String]))

    def writes(audience: Audience) = JsObject(Seq(
      "name" -> JsString(audience.name)))
  }

  def audiences = IsAuthenticated { adminid =>
    _ =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(
          html.audiences(
            Audience.findByAdmin(admin).asScala,
            admin))
      }.getOrElse(Forbidden)
  }

  def audienceJson(admin: Admin) : JsValue =
    Json.toJson(Audience.findByAdmin(admin).asScala)

  /** Action to get the audiences */
  def audienceList(page: Int, perPage: Int) = IsAuthenticated { adminid =>
    _ =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(audienceJson(admin))
      }.getOrElse(Forbidden)
  }
}