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
  import MainController.MessageFormat

  implicit object AudienceFormat extends Format[Audience] {
    def reads(json: JsValue) = JsSuccess(new Audience(
      (json \ "name").as[String]))

    def writes(audience: Audience) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(audience.id)),
      "name" -> JsString(audience.name)))
  }

  implicit object WebsiteFormat extends Format[Website] {
    def reads(json: JsValue) = JsSuccess(new Website(
      (json \ "name").as[String]))

    def writes(website: Website) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(website.id)),
      "url" -> JsString(website.url),
      "name" -> JsString(website.name)))
  }

  val audienceForm = Form(
    tuple(
      "id" -> text,
      "name" -> text))

  val websiteForm = Form(
    tuple(
      "id" -> text,
      "name" -> text))

  def audiences = IsAuthenticated { adminid =>
    request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(
          html.audiences(
            Audience.findByAdmin(admin).asScala,
            admin))
      }.getOrElse(Forbidden)
  }

  def audienceJson(admin: Admin): JsValue =
    Json.toJson(Audience.findByAdmin(admin).asScala)

  def websiteJson(admin: Admin): JsValue =
    Json.toJson(Website.findByAdmin(admin).asScala)

  /** Action to get the audiences */
  def audienceList(publisherid: String) = IsAuthenticated { adminid =>
    request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(audienceJson(admin))
      }.getOrElse(Forbidden)
  }

  def audienceSave(publisherid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        audienceForm.bindFromRequest.fold(
          (messages) => Forbidden,
          (data) => Audience.findById(data._1, admin).map { audience =>
            audience.save()
            Ok(JsObject(Seq(
              "data" -> Json.toJson(audience),
              "messages" -> Json.toJson(Seq[Message]()))))
          }.getOrElse {
            val audience = new Audience(data._2)
            val publisher = Publisher.findById(publisherid, admin)
            audience.publisher = publisher.get
            audience.save()
            Ok(JsObject(Seq(
              "data" -> Json.toJson(audience),
              "messages" -> Json.toJson(Seq[Message]()))))
          })
      }.getOrElse(Forbidden)
  }

  def audienceRemove(publisherid: String, audienceid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        audienceForm.bindFromRequest.fold(
          (messages) => Forbidden,
          (data) => Audience.findById(data._1, admin).map { audience =>
            Ok(JsObject(Seq(
              "data" -> Json.toJson(audience),
              "messages" -> Json.toJson(Seq[Message]()))))
          }.getOrElse(NotFound))
      }.getOrElse(Forbidden)
  }

  def websiteList(publisherid: String) = IsAuthenticated { adminid =>
    request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(websiteJson(admin))
      }.getOrElse(Forbidden)
  }

  def websiteSave(publisherid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        websiteForm.bindFromRequest.fold(
          (messages) => NotFound,
          (data) => Website.findById(data._1, admin).map { website =>
            website.save()
            Ok(JsObject(Seq(
              "data" -> Json.toJson(website),
              "messages" -> Json.toJson(Seq[Message]()))))
          }.getOrElse(Forbidden))
      }.getOrElse(Forbidden)
  }

  def websiteRemove(publisherid: String, websiteid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        websiteForm.bindFromRequest.fold(
          (messages) => NotFound,
          (data) => Website.findById(data._1, admin).map { website =>
            Ok(JsObject(Seq(
              "data" -> Json.toJson(website),
              "messages" -> Json.toJson(Seq[Message]()))))
          }.getOrElse(Forbidden))
      }.getOrElse(Forbidden)
  }
}