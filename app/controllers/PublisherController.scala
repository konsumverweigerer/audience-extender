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