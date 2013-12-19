package controllers

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import models._
import views._

import play.api._
import play.api.Play._
import play.api.data._
import play.api.data.Forms._

import play.api.libs.json._
import play.api.mvc._

import play.Logger

object PublisherController extends Controller with Secured with Formats with Utils {
  def uploadCreative = (publisherid: String) => IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Publisher.findById(publisherid, admin).map { publisher =>
          request.body.asMultipartFormData.map { body =>
            body.file("files[]").map { file =>
              Logger.debug("uploading " + file.filename + " to " + publisher)
              Logger.debug("contentType: " + file.contentType.getOrElse("application/octet-steam"))
              Logger.debug("file: " + file.ref.file)
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
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Ok(
          html.publishers(
            Publisher.findByAdmin(admin),
            admin))
      }.getOrElse(Forbidden)
  }

  def publisherJson(admin: Admin): JsValue =
    Json.toJson(Publisher.findByAdmin(admin).asScala)

  /** Action to get the publishers */
  def publisherList = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Ok(publisherJson(admin))
      }.getOrElse(Forbidden)
  }

  def publisherSave = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        request.body.asFormUrlEncoded.map { data =>
          data.get("id").map { ids =>
            Publisher.findById(ids(0), admin).map { publisher =>
              publisher.updateFromMap(mapToMap(data))
              val msgs = publisher.write().asScala
              Ok(JsObject(Seq(
                "data" -> Json.toJson(publisher),
                "messages" -> Json.toJson(msgs))))
            }.getOrElse(NotFound)
          }.getOrElse {
            val publisher = Publisher.fromMap(mapToMap(data))
            val msgs = publisher.write().asScala
            Ok(JsObject(Seq(
              "data" -> Json.toJson(publisher),
              "messages" -> Json.toJson(msgs))))
          }
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def message = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Ok(publisherJson(admin))
      }.getOrElse(Forbidden)
  }

  def dashboard = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Ok(Json.toJson(
          Publisher.statsByAdmin(admin).asScala))
      }.getOrElse(Forbidden)
  }

  def stats(publisherid: Long) = Action(parse.json) {
    implicit req =>
      Ok(Json.toJson(""))
  }
}