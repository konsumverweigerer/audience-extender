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
  val basicAdminForm: Form[Admin] = Form(
    mapping(
      "id" -> number,
      "name" -> text,
      "email" -> text)(
        (id: Int, name: String, email: String) => new Admin(email, name, null))(
          (admin: Admin) => Some(admin.id.toInt, admin.name, admin.email)))

  def adminJson(admin: Admin): JsValue =
    Json.toJson(Admin.findByAdmin(admin).asScala)

  def cookieJson(admin: Admin): JsValue =
    Json.toJson(models.Cookie.findByAdmin(admin).asScala)

  def creativeJson(admin: Admin): JsValue =
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

  def cookieList = HasRole("sysadmin") { adminid =>
    request =>
      Admin.findById(adminid).map { admin =>
        Ok(cookieJson(admin))
      }.getOrElse(Forbidden)
  }

  def creativeList = HasRole("sysadmin") { adminid =>
    request =>
      Admin.findById(adminid).map { admin =>
        Ok(creativeJson(admin))
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

  def changePublisher(publisherid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Admin.changePublisher(publisherid, admin).map { publisher =>
          Ok(
            Json.toJson(publisher))
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def current = IsAuthenticated { currentid =>
    implicit request =>
      Admin.findById(currentid).map { current =>
        basicAdminForm.fill(current)
        Ok(html.admin(current, current))
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