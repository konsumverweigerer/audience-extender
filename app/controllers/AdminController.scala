package controllers

import play.api.libs.json._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import scala.collection.JavaConverters._
import PublisherController.PublisherFormat

import models._
import views._

object AdminController extends Controller with Secured {
  implicit object AdminFormat extends Format[Admin] {
    def reads(json: JsValue) = JsSuccess(new Admin(
      (json \ "name").as[String],
      (json \ "email").as[String]))

    def writes(admin: Admin) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(admin.id)),
      "name" -> JsString(admin.name),
      "roles" -> Json.toJson(admin.getRoles().asScala),
      "publishers" -> Json.toJson(admin.publishers.asScala),
      "email" -> JsString(admin.email)))
  }

  val basicAdminForm: Form[Admin] = Form(
    mapping(
      "name" -> text,
      "email" -> text)(
        (name: String, email: String) => new Admin(email, name, null))(
          (admin: Admin) => Some(admin.name, admin.email)))

  def adminJson(admin: Admin): JsValue =
    Json.toJson(Admin.findByAdmin(admin).asScala)

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

  /** Action to get the publishers */
  def adminList(page: Int, perPage: Int) = IsAuthenticated { adminid =>
    _ => Option[Admin](Admin.findById(adminid)).map { admin =>
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