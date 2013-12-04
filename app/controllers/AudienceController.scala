package controllers

import scala.collection.JavaConverters.asScalaBufferConverter

import models.Admin
import models.Audience
import models.Message
import models.Publisher
import models.Website
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.Controller
import views.html

object AudienceController extends Controller with Secured with Formats with Utils {
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
        request.body.asFormUrlEncoded.map { data =>
          data.get("id").map { ids =>
            Audience.findById(ids(0), admin).map { audience =>
              audience.updateFromMap(mapToMap(data))
              audience.save()
              Ok(JsObject(Seq(
                "data" -> Json.toJson(audience),
                "messages" -> Json.toJson(Seq[Message]()))))
            }.getOrElse(NotFound)
          }.getOrElse {
            val audience = Audience.fromMap(mapToMap(data))
            val publisher = Publisher.findById(publisherid, admin)
            audience.publisher = publisher.get
            audience.save()
            Ok(JsObject(Seq(
              "data" -> Json.toJson(audience),
              "messages" -> Json.toJson(Seq[Message]()))))
          }
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def audienceRemove(publisherid: String, audienceid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Audience.findById(audienceid, admin).map { audience =>
          audience.remove()
          audience.save()
          Ok(JsObject(Seq(
            "data" -> Json.toJson(audience),
            "messages" -> Json.toJson(Seq[Message]()))))
        }.getOrElse(NotFound)
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
        request.body.asFormUrlEncoded.map { data =>
          data.get("id").map { ids =>
            Website.findById(ids(0), admin).map { website =>
              website.updateFromMap(mapToMap(data))
              website.save()
              Ok(JsObject(Seq(
                "data" -> Json.toJson(website),
                "messages" -> Json.toJson(Seq[Message]()))))
            }.getOrElse(NotFound)
          }.getOrElse {
            val website = Website.fromMap(mapToMap(data))
            val publisher = Publisher.findById(publisherid, admin)
            website.publisher = publisher.get
            website.save()
            Ok(JsObject(Seq(
              "data" -> Json.toJson(website),
              "messages" -> Json.toJson(Seq[Message]()))))
          }
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def websiteRemove(publisherid: String, websiteid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Website.findById(websiteid, admin).map { website =>
          website.remove()
          website.save()
          Ok(JsObject(Seq(
            "data" -> Json.toJson(website),
            "messages" -> Json.toJson(Seq[Message]()))))
        }.getOrElse(NotFound)
      }.getOrElse(Forbidden)
  }
}