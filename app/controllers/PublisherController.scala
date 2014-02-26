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
        publisherForm.bindFromRequest.fold(
          errors => {
            val msgs = Seq(new Message("error", errors.globalError.map(e => e.message).getOrElse("error"), "error"))
            BadRequest(JsObject(Seq(
              "data" -> Json.toJson(Map[String, String]()),
              "messages" -> Json.toJson(msgs))))
          },
          data =>
            Some(data._1).map { id =>
              Publisher.findById(id, admin).map { publisher =>
                publisher.setName(data._2)
                data._4.map { url => publisher.setUrl(url) }
                data._5.map { s => publisher.setStreetaddress1(s) }
                data._6.map { s => publisher.setStreetaddress2(s) }
                data._7.map { s => publisher.setStreetaddress3(s) }
                data._8.map { s => publisher.setState(s) }
                data._9.map { c => publisher.setCountry(c) }
                data._10.map { t => publisher.setTelephone(t) }
                val msgs = publisher.write().asScala
                Ok(JsObject(Seq(
                  "data" -> Json.toJson(publisher),
                  "messages" -> Json.toJson(msgs))))
              }.getOrElse(NotFound)
            }.getOrElse {
              val publisher = new Publisher(data._2)
              data._4.map { url => publisher.setUrl(url) }
              data._5.map { s => publisher.setStreetaddress1(s) }
              data._6.map { s => publisher.setStreetaddress2(s) }
              data._7.map { s => publisher.setStreetaddress3(s) }
              data._8.map { s => publisher.setState(s) }
              data._9.map { c => publisher.setCountry(c) }
              data._10.map { t => publisher.setTelephone(t) }
              val msgs = publisher.write().asScala
              Ok(JsObject(Seq(
                "data" -> Json.toJson(publisher),
                "messages" -> Json.toJson(msgs))))
            })
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