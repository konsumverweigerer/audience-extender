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

object AdminController extends Controller with Secured with Formats with Utils {
  def NullSome[A](t: A) = if (t != null) Some(t) else None

  val basicAdminForm: Form[Admin] = Form(
    mapping(
      "id" -> longNumber,
      "name" -> text,
      "email" -> email,
      "url" -> optional(text),
      "streetaddress1" -> optional(text),
      "streetaddress2" -> optional(text),
      "streetaddress3" -> optional(text),
      "state" -> optional(text),
      "country" -> optional(text),
      "telephone" -> optional(text))(
        (id, name, email, url,
          streetaddress1, streetaddress2, streetaddress3, state, country, telephone) =>
          new Admin(email, name, null))(
          (admin: Admin) => {
            val id: Long = admin.getId
            val t = (id, admin.getName, admin.getEmail, NullSome(admin.getUrl), NullSome(admin.getStreetaddress1), NullSome(admin.getStreetaddress2),
              NullSome(admin.getStreetaddress3), NullSome(admin.getState), NullSome(admin.getCountry), NullSome(admin.getTelephone))
            Some(t)
          }))

  def adminJson(admin: Admin): JsValue =
    Json.toJson(Admin.findByAdmin(admin).asScala)

  def cookieJson(publisherid: String, admin: Admin): JsValue =
    Json.toJson(models.Cookie.findByAdmin(admin).asScala)

  def creativeJson(publisherid: String, admin: Admin): JsValue =
    Json.toJson(Creative.findByAdmin(admin).asScala)

  def deleteAdmin(adminid: String) = HasRole("sysadmin") { currentid =>
    implicit request =>
      Admin.findById(currentid).map { current =>
        Admin.findById(adminid).map { admin =>
          admin.delete()
          Ok(html.admins(
            Admin.findByAdmin(current),
            Publisher.findByAdmin(admin).asScala,
            current))
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def admins = HasRole("sysadmin") { adminid =>
    request =>
      Admin.findById(adminid).map { admin =>
        Ok(html.admins(
          Admin.findByAdmin(admin).asScala,
          Publisher.findByAdmin(admin).asScala,
          admin))
      }.getOrElse(Forbidden)
  }

  def creatives = HasRole("sysadmin") { adminid =>
    request =>
      Admin.findById(adminid).map { admin =>
        Ok(html.creatives(
          Creative.findByAdmin(admin).asScala,
          Publisher.findByAdmin(admin).asScala,
          admin))
      }.getOrElse(Forbidden)
  }

  def cookies = HasRole("sysadmin") { adminid =>
    request =>
      Admin.findById(adminid).map { admin =>
        Ok(html.cookies(
          models.Cookie.findByAdmin(admin).asScala,
          Publisher.findByAdmin(admin).asScala,
          admin))
      }.getOrElse(Forbidden)
  }

  /** Action to get the publishers */
  def adminList = HasRole("sysadmin") { adminid =>
    request =>
      Admin.findById(adminid).map { admin =>
        Ok(adminJson(admin))
      }.getOrElse(Forbidden)
  }

  def cookieList(publisherid: String) = HasRole("sysadmin") { adminid =>
    request =>
      Admin.findById(adminid).map { admin =>
        Ok(cookieJson(publisherid, admin))
      }.getOrElse(Forbidden)
  }

  def creativeList(publisherid: String) = HasRole("sysadmin") { adminid =>
    request =>
      Admin.findById(adminid).map { admin =>
        Ok(creativeJson(publisherid, admin))
      }.getOrElse(Forbidden)
  }

  def adminSave = IsAuthenticated { currentid =>
    implicit request =>
      Admin.findById(currentid).map { current =>
        adminForm.bindFromRequest.fold(
          errors => {
            val msgs = Seq(new Message("error", errors.globalError.map(e => e.message).getOrElse("error"), "error"))
            BadRequest(JsObject(Seq(
              "data" -> Json.toJson(Map[String, String]()),
              "messages" -> Json.toJson(msgs))))
          },
          data =>
            Some(data._1).map { id =>
              Admin.findById(id).map { admin =>
                //TODO: fill from form
                val msgs = admin.write()
                Ok(JsObject(Seq(
                  "data" -> Json.toJson(admin),
                  "messages" -> Json.toJson(msgs.asScala))))
              }.getOrElse(NotFound)
            }.getOrElse {
              val admin = new Admin()
              //TODO: fill from form
              val msgs = admin.write()
              Ok(JsObject(Seq(
                "data" -> Json.toJson(admin),
                "messages" -> Json.toJson(msgs.asScala))))
            })
      }.getOrElse(Forbidden)
  }

  def creativeSave = IsAuthenticated { currentid =>
    implicit request =>
      Admin.findById(currentid).map { current =>
        creativeForm.bindFromRequest.fold(
          errors => {
            val msgs = Seq(new Message("error", errors.globalError.map(e => e.message).getOrElse("error"), "error"))
            BadRequest(JsObject(Seq(
              "data" -> Json.toJson(Map[String, String]()),
              "messages" -> Json.toJson(msgs))))
          },
          data =>
            Some(data._1).map { id =>
              Admin.findById(id).map { admin =>
                //TODO: fill from form
                val msgs = admin.write()
                Ok(JsObject(Seq(
                  "data" -> Json.toJson(admin),
                  "messages" -> Json.toJson(msgs.asScala))))
              }.getOrElse(NotFound)
            }.getOrElse {
              val admin = new Admin()
              //TODO: fill from form
              val msgs = admin.write()
              Ok(JsObject(Seq(
                "data" -> Json.toJson(admin),
                "messages" -> Json.toJson(msgs.asScala))))
            })
      }.getOrElse(Forbidden)
  }

  def cookieSave = IsAuthenticated { currentid =>
    implicit request =>
      Admin.findById(currentid).map { current =>
        cookieForm.bindFromRequest.fold(
          errors => {
            val msgs = Seq(new Message("error", errors.globalError.map(e => e.message).getOrElse("error"), "error"))
            BadRequest(JsObject(Seq(
              "data" -> Json.toJson(Map[String, String]()),
              "messages" -> Json.toJson(msgs))))
          },
          data =>
            Some(data._1).map { id =>
              models.Cookie.findById(id, current).map { cookie =>
                if (data._3.nonEmpty) {
                  cookie.setContent(data._3.get)
                  cookie.setState("A")
                }
                val msgs = cookie.write()
                Ok(JsObject(Seq(
                  "data" -> Json.toJson(cookie),
                  "messages" -> Json.toJson(msgs.asScala))))
              }.getOrElse(NotFound)
            }.getOrElse {
              val cookie = new models.Cookie(data._2)
              if (data._3.nonEmpty) {
                cookie.setContent(data._3.get)
                cookie.setState("A")
              }
              val msgs = cookie.write()
              Ok(JsObject(Seq(
                "data" -> Json.toJson(cookie),
                "messages" -> Json.toJson(msgs.asScala))))
            })
      }.getOrElse(Forbidden)
  }

  /** Action to save a admin */
  def saveAdmin(adminid: String) = HasRole("sysadmin") { currentid =>
    implicit request =>
      Admin.findById(currentid).map { current =>
        Admin.findById(adminid).map { admin =>
          basicAdminForm.bindFromRequest.fold(
            (messages) => {
              //TODO: copy changes
              Ok(html.admin(admin, current))
            },
            (admindata) => {
              //TODO: copy changes
              admin.save()
              Ok(html.admin(admin, current))
            })
        }.getOrElse(NotFound)
      }.getOrElse(Forbidden)
  }

  def admin(adminid: String) = HasRole("sysadmin") { currentid =>
    implicit request =>
      Admin.findById(currentid).map { current =>
        Admin.findById(adminid).map { admin =>
          basicAdminForm.fill(admin)
          Ok(html.admin(admin, current))
        }.getOrElse(NotFound)
      }.getOrElse(Forbidden)
  }

  def addAdmin = HasRole("sysadmin") { currentid =>
    implicit request =>
      Admin.findById(currentid).map { current =>
        Option(Admin.newAdmin(current)).map { admin =>
          basicAdminForm.fill(admin)
          Ok(html.admin(admin, current))
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def attachPublisher(adminid: String, publisherid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Admin.changePublisher(publisherid, admin).map { publisher =>
          publisher.getOwners().add(admin)
          val msgs = publisher.write().asScala
          Ok(Json.toJson(JsObject(Seq(
            "data" -> Json.toJson(msgs.isEmpty()),
            "messages" -> Json.toJson(msgs)))))
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def detachPublisher(adminid: String, publisherid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Admin.changePublisher(publisherid, admin).map { publisher =>
          publisher.getOwners().remove(admin)
          if (admin.getPublisher.equals(publisher)) { admin.setPublisher(null) }
          val msgs = (admin.write().asScala)
          msgs.addAll(publisher.write().asScala)
          Ok(Json.toJson(JsObject(Seq(
            "data" -> Json.toJson(msgs.isEmpty()),
            "messages" -> Json.toJson(msgs)))))
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def changePublisher(publisherid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Admin.changePublisher(publisherid, admin).map { publisher =>
          Ok(Json.toJson(publisher))
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def current = IsAuthenticated { currentid =>
    implicit request =>
      Admin.findById(currentid).map { current =>
        basicAdminForm.fill(current)
        Ok(html.current(Seq(current), current))
      }.getOrElse(Forbidden)
  }

  def saveCurrent = IsAuthenticated { currentid =>
    implicit request =>
      Admin.findById(currentid).map { current =>
        basicAdminForm.bindFromRequest.fold(
          (messages) => {
            //TODO: copy changes
            Ok(html.admin(current, current))
          },
          (admindata) => {
            //TODO: copy changes
            current.save()
            Ok(html.admin(current, current))
          })
      }.getOrElse(NotFound)
  }
}