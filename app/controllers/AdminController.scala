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
      "name" -> text,
      "email" -> text)(
        (name: String, email: String) => new Admin(email, name, null))(
          (admin: Admin) => Some(admin.name, admin.email)))

  def adminJson(admin: Admin): JsValue =
    Json.toJson(Admin.findByAdmin(admin).asScala)

  def deleteAdmin(adminid: Long) = HasRole("sysadmin") { admin =>
    request =>
      Admin.delete(adminid)
      Ok
  }

  def admins = IsAuthenticated { adminid =>
    request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(
          html.admins(
            Admin.findByAdmin(admin).asScala,
            admin))
      }.getOrElse(Forbidden)
  }

  /** Action to get the publishers */
  def adminList(page: Int, perPage: Int) = IsAuthenticated { adminid =>
    request => Option[Admin](Admin.findById(adminid)).map { admin =>
      Ok(adminJson(admin))
    }.getOrElse(Forbidden)
  }

  /** Action to save a admin */
  def saveAdmin(adminid: Long) = Action(parse.json) { implicit req =>
    Ok(Json.toJson(""))
  }

  def admin(adminid: Long) = Action(parse.json) { implicit req =>
    Ok(Json.toJson(""))
  }

  def addAdmin = IsAuthenticated { adminid =>
    request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        val newAdmin = Admin.newAdmin(admin)
        if (newAdmin != null) {
          Ok(
            html.admin(newAdmin,
              admin))
        } else {
          Forbidden
        }
      }.getOrElse(Forbidden)
  }

  def changePublisher(publisherid: String) = IsAuthenticated { adminid =>
    request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Admin.changePublisher(publisherid, admin).map { publisher =>
          Ok(
            Json.toJson(publisher))
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def current = IsAuthenticated { adminid =>
    request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(
          html.current(
            admin))
      }.getOrElse(Forbidden)
  }

  def saveCurrent = Action(parse.json) { implicit req =>
    Ok(Json.toJson(""))
  }
}