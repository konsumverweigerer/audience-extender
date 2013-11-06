package controllers

import play.api.libs.json._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import scala.collection.JavaConverters._

import models._
import views._

object AdminController extends Controller with Secured {
  val basicAdminForm: Form[Admin] = Form(
    mapping(
      "name" -> text,
      "email" -> text)(
        (name: String, email: String) => new Admin(email, name, null))(
          (admin: Admin) => Some(admin.name, admin.email)))

  def deleteAdmin(adminid: Long) = HasRole("sysadmin") { admin =>
    _ =>
      Admin.delete(adminid)
      Ok
  }

  def admins = IsAuthenticated { adminid =>
    _ =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(
          html.admins(
            Admin.findByAdmin(admin).asScala,
            admin))
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
    _ =>
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

  def current = IsAuthenticated { adminid =>
    _ =>
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