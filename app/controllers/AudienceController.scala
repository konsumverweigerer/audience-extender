package controllers

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

import models._
import views._

import play.api._
import play.api.Play._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.mvc._
import play.Logger

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
            val msgs = ArrayBuffer[Message]()
            errors.globalError.map { e =>
              msgs.add(new Message("Could not save", e.message, "error"))
            }
            msgs.addAll(errors.errors.map { e =>
              new Message(e.key, e.message, "error")
            })
            BadRequest(JsObject(Seq(
              "data" -> Json.toJson(Map[String, String]()),
              "messages" -> Json.toJson(msgs))))
          },
          data =>
            data._1.filter(i => i > 0).map { id =>
              Audience.findById(id, admin).map { audience =>
                audience.name = data._2
                audience.tracking = data._3.getOrElse(null)
                if (data._5 != null) {
                  audience.websites.clear()
                  data._5.map { t =>
                    Website.findById(t._1.getOrElse(-1L), admin).map { website =>
                      audience.websites.add(website)
                      val l = audience.pathTargets.filter(p => "*".equals(p.urlPath) && t._1.equals(p.website.id))
                      if (l.isEmpty()) {
                        val tg = new PathTarget("*")
                        tg.variant = if (t._2) "include" else "exclude"
                        tg.audience = audience
                        tg.website = website
                        audience.pathTargets.add(tg)
                      } else {
                        l.map { tg =>
                          tg.variant = if (t._2) "include" else "exclude"
                        }
                      }
                    }
                  }
                }
                if (data._4 != null) {
                  data._4.map { t =>
                    val ids = audience.pathTargets.map(p => p.id)
                    Website.findById(t._2, admin).map { website =>
                      if (t._1.getOrElse(-1L) > 0) {
                        audience.pathTargets.filter(p => t._1.equals(p.id)).map { tg =>
                          tg.variant = if (t._4) "include" else "exclude"
                        }
                      } else {
                        val tg = new PathTarget(t._3)
                        tg.website = website
                        tg.variant = if (t._4) "include" else "exclude"
                        tg.audience = audience
                        audience.pathTargets.add(tg)
                      }
                    }
                    audience.pathTargets.filter(p => p.id != null && !ids.contains(p.id)).map { tg =>
                      tg.delete();
                    }
                  }
                }
                val msgs = audience.write().asScala
                Ok(JsObject(Seq(
                  "data" -> Json.toJson(audience),
                  "messages" -> Json.toJson(msgs))))
              }.getOrElse(NotFound)
            }.getOrElse {
              val audience = new Audience("")
              audience.name = data._2
              audience.state = "P"
              audience.tracking = data._3.getOrElse(null)
              if (data._5 != null) {
                data._5.map { t =>
                  Website.findById(t._1.getOrElse(-1L), admin).map { website =>
                    audience.websites.add(website)
                    val tg = new PathTarget("*")
                    tg.variant = if (t._2) "include" else "exclude"
                    tg.audience = audience
                    tg.website = website
                    audience.pathTargets.add(tg)
                  }
                }
              }
              if (data._4 != null) {
                data._4.map { t =>
                  Website.findById(t._2, admin).map { website =>
                    val tg = new PathTarget(t._3)
                    tg.website = website
                    tg.variant = if (t._4) "include" else "exclude"
                    tg.audience = audience
                    audience.pathTargets.add(tg)
                  }
                }
              }
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
        websiteForm.bindFromRequest.fold(
          errors => {
            val msgs = ArrayBuffer[Message]()
            errors.globalError.map { e =>
              msgs.add(new Message("Could not save", e.message, "error"))
            }
            msgs.addAll(errors.errors.map { e =>
              new Message(e.key, e.message, "error")
            })
            BadRequest(JsObject(Seq(
              "data" -> Json.toJson(Map[String, String]()),
              "messages" -> Json.toJson(msgs))))
          },
          data =>
            data._1.filter(i => i > 0).map { id =>
              Website.findById(id, admin).map { website =>
                website.name = data._2
                website.url = data._3
                val msgs = website.write().asScala
                Ok(JsObject(Seq(
                  "data" -> Json.toJson(website),
                  "messages" -> Json.toJson(msgs))))
              }.getOrElse(NotFound)
            }.getOrElse {
              val website = new Website("")
              website.name = data._2
              website.url = data._3
              val publisher = Publisher.findById(publisherid, admin)
              website.publisher = publisher.get
              val msgs = website.write().asScala
              Ok(JsObject(Seq(
                "data" -> Json.toJson(website),
                "messages" -> Json.toJson(msgs))))
            })
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

  def dashboard(from: Long, to: Long) = IsAuthenticated { adminid =>
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