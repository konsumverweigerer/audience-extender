package controllers

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import models._
import play.api._
import play.api.Play._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.mvc._
import views._

object AudienceController extends Controller with Secured with Formats with Utils {
  def audiences = IsAuthenticated { adminid =>
    request =>
      Admin.findById(adminid).map { admin =>
        Ok(
          html.audiences(
            Audience.findByAdmin(admin).asScala,
            admin))
      }.getOrElse(Forbidden)
  }

  def audienceJson(admin: Admin, publisherid: String): JsValue =
    Json.toJson(Audience.findByAdmin(admin).asScala)

  def websiteJson(admin: Admin, publisherid: String): JsValue =
    Json.toJson(Website.findByAdmin(admin).asScala)

  /** Action to get the audiences */
  def audienceList(publisherid: String) = IsAuthenticated { adminid =>
    request =>
      Admin.findById(adminid).map { admin =>
        Ok(audienceJson(admin, publisherid))
      }.getOrElse(Forbidden)
  }

  def audienceSave(publisherid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        audienceForm.bindFromRequest.fold(
          errors => {
            val msgs = Seq(new Message("error", errors.globalError.map(e => e.message).getOrElse("error"), "error"))
            BadRequest(JsObject(Seq(
              "data" -> Json.toJson(Map[String, String]()),
              "messages" -> Json.toJson(msgs))))
          },
          data =>
            Some(data._1).map { id =>
              Audience.findById(id, admin).map { audience =>
                //TODO: fill from form
                val msgs = audience.write().asScala
                Ok(JsObject(Seq(
                  "data" -> Json.toJson(audience),
                  "messages" -> Json.toJson(msgs))))
              }.getOrElse(NotFound)
            }.getOrElse {
              val audience = new Audience("")
              //TODO: fill from form
              val publisher = Publisher.findById(publisherid, admin)
              audience.publisher = publisher.get
              val msgs = audience.write().asScala
              Ok(JsObject(Seq(
                "data" -> Json.toJson(audience),
                "messages" -> Json.toJson(msgs))))
            })
      }.getOrElse(Forbidden)
  }

  def audienceRemove(publisherid: String, audienceid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Audience.findById(audienceid, admin).map { audience =>
          val msgs = audience.remove().asScala
          if (msgs.isEmpty()) {
            audience.save()
          }
          Ok(JsObject(Seq(
            "data" -> Json.toJson(audience),
            "messages" -> Json.toJson(msgs))))
        }.getOrElse(NotFound)
      }.getOrElse(Forbidden)
  }

  def websiteList(publisherid: String) = IsAuthenticated { adminid =>
    request =>
      Admin.findById(adminid).map { admin =>
        Ok(websiteJson(admin, publisherid))
      }.getOrElse(Forbidden)
  }

  def websiteSave(publisherid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        request.body.asFormUrlEncoded.map { data =>
          data.get("id").map { ids =>
            Website.findById(ids(0), admin).map { website =>
              website.updateFromMap(mapToMap(data))
              val msgs = website.write().asScala
              Ok(JsObject(Seq(
                "data" -> Json.toJson(website),
                "messages" -> Json.toJson(msgs))))
            }.getOrElse(NotFound)
          }.getOrElse {
            val website = Website.fromMap(mapToMap(data))
            val publisher = Publisher.findById(publisherid, admin)
            website.publisher = publisher.get
            val msgs = website.write().asScala
            Ok(JsObject(Seq(
              "data" -> Json.toJson(website),
              "messages" -> Json.toJson(msgs))))
          }
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def websiteRemove(publisherid: String, websiteid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Website.findById(websiteid, admin).map { website =>
          val msgs = website.remove()
          if (msgs.isEmpty()) {
            website.save()
          }
          Ok(JsObject(Seq(
            "data" -> Json.toJson(website),
            "messages" -> Json.toJson(msgs.asScala))))
        }.getOrElse(NotFound)
      }.getOrElse(Forbidden)
  }

  def dashboard(from: String, to: String) = IsAuthenticated { adminid =>
    _ =>
      Admin.findById(adminid).map { admin =>
        Ok(Json.toJson(
          Audience.statsByAdmin(admin, from, to).asScala))
      }.getOrElse(Forbidden)
  }

  def stats(audienceid: Long) = Action(parse.json) { implicit req =>
    Ok(Json.toJson(""))
  }
}