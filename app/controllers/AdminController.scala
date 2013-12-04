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

  def deleteAdmin(adminid: String) = HasRole("sysadmin") { currentid =>
    implicit request =>
      Admin.findById(currentid).map { current =>
        Admin.findById(adminid).map { admin =>
          admin.delete()
          Ok(html.admins(Admin.findByAdmin(current), current))
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def admins = HasRole("sysadmin") { adminid =>
    request =>
      Admin.findById(adminid).map { admin =>
        Ok(
          html.admins(
            Admin.findByAdmin(admin).asScala,
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

  def adminSave = IsAuthenticated { currentid =>
    implicit request =>
      Admin.findById(currentid).map { current =>
        request.body.asFormUrlEncoded.map { data =>
          data.get("id").map { ids =>
            Admin.findById(ids(0)).map { admin =>
              admin.updateFromMap(mapToMap(data))
              val msgs = admin.write()
              Ok(JsObject(Seq(
                "data" -> Json.toJson(admin),
                "messages" -> Json.toJson(msgs.asScala))))
            }.getOrElse(NotFound)
          }.getOrElse {
            val admin = Admin.fromMap(mapToMap(data))
            val msgs = admin.write()
            Ok(JsObject(Seq(
              "data" -> Json.toJson(admin),
              "messages" -> Json.toJson(msgs.asScala))))
          }
        }.getOrElse(Forbidden)
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