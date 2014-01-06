package controllers

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

import models._
import views._
import services._

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
                audience.setName(data._2)
                audience.setTracking(data._3.getOrElse(null))
                if (data._5 != null) {
                  audience.getWebsites.clear
                  data._5.map { t =>
                    Website.findById(t._1.getOrElse(-1L), admin).map { website =>
                      audience.getWebsites.add(website)
                      val l = audience.getPathTargets.filter(p => "*".equals(p.getUrlPath) && t._1.getOrElse(-1).equals(p.getWebsite.getId))
                      if (l.isEmpty()) {
                        val tg = new PathTarget("*")
                        tg.setVariant(if (t._2) "include" else "exclude")
                        tg.setAudience(audience)
                        tg.setWebsite(website)
                        audience.getPathTargets.add(tg)
                      } else {
                        l.map { tg =>
                          tg.setVariant(if (t._2) "include" else "exclude")
                        }
                      }
                    }
                  }
                }
                if (data._4 != null) {
                  data._4.map { t =>
                    val ids = audience.getPathTargets.map(p => p.getId)
                    Website.findById(t._2, admin).map { website =>
                      if (t._1.getOrElse(-1L) > 0) {
                        audience.getPathTargets.filter(p => t._1.getOrElse(-1).equals(p.getId)).map { tg =>
                          tg.setVariant(if (t._4) "include" else "exclude")
                        }
                      } else {
                        val tg = new PathTarget(t._3)
                        tg.setWebsite(website)
                        tg.setVariant(if (t._4) "include" else "exclude")
                        tg.setAudience(audience)
                        audience.getPathTargets.add(tg)
                      }
                    }
                    audience.getPathTargets.filter(p => p.getId != null && !ids.contains(p.getId)).map { tg =>
                      tg.delete();
                    }
                  }
                }
                val msgs = audience.write.asScala
                Ok(JsObject(Seq(
                  "data" -> Json.toJson(audience),
                  "messages" -> Json.toJson(msgs))))
              }.getOrElse(NotFound)
            }.getOrElse {
              val audience = new Audience("")
              audience.setName(data._2)
              audience.setState("P")
              audience.setTracking(data._3.getOrElse(null))
              if (data._5 != null) {
                data._5.map { t =>
                  Website.findById(t._1.getOrElse(-1L), admin).map { website =>
                    audience.getWebsites.add(website)
                    val tg = new PathTarget("*")
                    tg.setVariant(if (t._2) "include" else "exclude")
                    tg.setAudience(audience)
                    tg.setWebsite(website)
                    audience.getPathTargets.add(tg)
                  }
                }
              }
              if (data._4 != null) {
                data._4.map { t =>
                  Website.findById(t._2, admin).map { website =>
                    val tg = new PathTarget(t._3)
                    tg.setWebsite(website)
                    tg.setVariant(if (t._4) "include" else "exclude")
                    tg.setAudience(audience)
                    audience.getPathTargets.add(tg)
                  }
                }
              }
              val publisher = Publisher.findById(publisherid, admin)
              audience.setPublisher(publisher.get)
              val msgs = audience.write.asScala
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
          val msgs = audience.remove.asScala
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
                website.setName(data._2)
                website.setUrl(data._3)
                val msgs = website.write.asScala
                Ok(JsObject(Seq(
                  "data" -> Json.toJson(website),
                  "messages" -> Json.toJson(msgs))))
              }.getOrElse(NotFound)
            }.getOrElse {
              val website = new Website("")
              website.setName(data._2)
              website.setUrl(data._3)
              val publisher = Publisher.findById(publisherid, admin)
              website.setPublisher(publisher.get)
              val msgs = website.write.asScala
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

  def sendWebsiteCode(email: String, publisherid: String, websiteid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Website.findById(websiteid, admin).map { website =>
          val msgs = ArrayBuffer[Message]()
          val res = SendMail.sendWebsiteCodeEmail(current, email, website.code(current))
          if (res != null && res) {
            msgs.add(new Message("Code E-mail", "E-mail sent successfully sent to " + email, "success"))
          } else {
            msgs.add(new Message("Code E-mail", "Could not send e-mail to " + email + ", check email address", "error"))
          }
          Ok(JsObject(Seq(
            "data" -> Json.toJson(website),
            "messages" -> Json.toJson(msgs))))
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