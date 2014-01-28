package controllers

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.joda.time.format.{ DateTimeFormat, DateTimeFormatter }

import models._
import services._
import views._

import play.Logger
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format._
import play.api.data.validation._
import play.api.data.validation.Constraints._
import play.api.libs.json._
import play.api.mvc._
import play.api.Play._

object MainController extends Controller with Secured with Formats with Utils {
  import play.api.Play.current

  val loginForm = Form(
    tuple(
      "email" -> (email verifying nonEmpty),
      "password" -> nonEmptyText))

  val contactForm = Form(
    tuple(
      "email" -> (email verifying nonEmpty),
      "name" -> nonEmptyText,
      "msg" -> nonEmptyText))

  val forgotPasswordForm = Form(
    tuple(
      "email" -> (email verifying nonEmpty),
      "name" -> text)
      verifying ("Unknown email address", result => result match {
        case (email, name) => (Admin.forgotPassword(email) != null)
      }))

  /**
   * Login page.
   */
  def login = Action { implicit request =>
    Ok(html.login(loginForm))
  }

  def policy = CheckIfIsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Ok(html.policy(admin))
      }.getOrElse(
        Ok(html.policy(null)))
  }

  def tos = CheckIfIsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Ok(html.tos(admin))
      }.getOrElse(
        Ok(html.tos(null)))
  }

  /**
   * Contact page.
   */
  def contact = CheckIfIsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Ok(html.contact(contactForm, admin))
      }.getOrElse(
        Ok(html.contact(contactForm, null)))
  }

  def sendMessage = CheckIfIsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        contactForm.bindFromRequest.fold(
          formWithErrors => BadRequest(html.contact(formWithErrors, admin)),
          message => {
            if (SendMail.sendContactMessage(current, message._1, message._2, message._3)) {
              Redirect(routes.MainController.contact).flashing("success" -> "Your message was sent")
            } else {
              Redirect(routes.MainController.contact).flashing("error" -> "Could not send message")
            }
          })
      }.getOrElse(
        contactForm.bindFromRequest.fold(
          formWithErrors => BadRequest(html.contact(formWithErrors, null)),
          message => {
            if (SendMail.sendContactMessage(current, message._1, message._2, message._3)) {
              Redirect(routes.MainController.contact).flashing("success" -> "Your message was sent")
            } else {
              Redirect(routes.MainController.contact).flashing("error" -> "Could not send message")
            }
          }))
  }

  def sendContactMessage = Action {
    implicit request =>
      contactForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.index(null, formWithErrors)),
        message => {
          if (SendMail.sendContactMessage(current, message._1, message._2, message._3)) {
            Redirect(routes.MainController.index(request.path)).flashing("success" -> "Your message was sent")
          } else {
            Redirect(routes.MainController.index(request.path)).flashing("error" -> "Could not send message")
          }
        })
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => {
        Admin.authenticate(user._1, user._2).map { admin =>
          admin.login(request.id.toString)
          Logger.info("logging in: " + user._1)
          Redirect(routes.MainController.dashboard).withSession(
            Security.username -> admin.getId.toString(),
            LOGINDATE -> loginDf.print({ new java.util.Date }.getTime))
        }.getOrElse {
          BadRequest(html.login(loginForm.fillAndValidate(user).withError("", "Invalid email or password")))
        }
      })
  }

  def forgotPassword = Action { implicit request =>
    forgotPasswordForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => {
        Redirect(routes.MainController.login).flashing("success" -> "Password reset e-mail sent, check your mail and follow the instructions in the e-mail to reset your password")
      })
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.MainController.index("")).withNewSession.flashing(
      "success" -> "You've been logged out")
  }

  /**
   * The index page.  This is the main entry point, seeing as this is a single page app.
   */
  def index(path: String) = Action { implicit request =>
    request.session.get(Security.username).map {
      id => Ok(html.index(Admin.findById(id).orNull, contactForm))
    }.getOrElse(
      Ok(html.index(Admin.findByEmail(""), contactForm)))
  }

  def base = index("")

  def dashboardAudience = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Ok(html.dashboardaudience(Publisher.findByAdmin(admin).asScala, admin))
      }.getOrElse(
        Ok(html.index(Admin.findByEmail(""), contactForm)))
  }

  def dashboardCampaign = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Ok(html.dashboardcampaign(Publisher.findByAdmin(admin).asScala, admin))
      }.getOrElse(
        Ok(html.index(Admin.findByEmail(""), contactForm)))
  }

  def dashboard = dashboardAudience

  def dashboardSelect(t: String = "audience") = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        if ("campaign".equals(t)) {
          Ok(html.dashboardcampaign(Publisher.findByAdmin(admin).asScala, admin))
        } else {
          Ok(html.dashboardaudience(Publisher.findByAdmin(admin).asScala, admin))
        }
      }.getOrElse(Ok(html.index(Admin.findByEmail(""), contactForm)))
  }

  /** The javascript router. */
  def javascriptRoutes = Action { implicit req =>
    Ok(
      Routes.javascriptRouter("routes")(
        routes.javascript.PublisherController.publisherList,
        routes.javascript.PublisherController.dashboard,
        routes.javascript.PublisherController.stats,
        routes.javascript.CampaignController.dashboard,
        routes.javascript.AudienceController.dashboard,
        routes.javascript.PublisherController.uploadCreative,
        routes.javascript.CampaignController.campaignList,
        routes.javascript.CampaignController.campaignSave,
        routes.javascript.CampaignController.creativeSave,
        routes.javascript.CampaignController.campaignRemove,
        routes.javascript.CampaignController.packageList,
        routes.javascript.CampaignController.packageSave,
        routes.javascript.CampaignController.packageRemove,
        routes.javascript.AudienceController.audienceList,
        routes.javascript.AudienceController.audienceSave,
        routes.javascript.AudienceController.audienceRemove,
        routes.javascript.AudienceController.websiteList,
        routes.javascript.AudienceController.websiteSave,
        routes.javascript.AudienceController.websiteRemove,
        routes.javascript.AudienceController.sendWebsiteCode,
        routes.javascript.AdminController.changePublisher,
        routes.javascript.AdminController.attachPublisher,
        routes.javascript.AdminController.detachPublisher,
        routes.javascript.AdminController.adminList,
        routes.javascript.AdminController.adminSave,
        routes.javascript.AdminController.cookieList,
        routes.javascript.AdminController.cookieSave,
        routes.javascript.AdminController.creativeList,
        routes.javascript.AdminController.creativeSave)).as("text/javascript")
  }

  def minProdWebJarAssetsAt(file: String): Action[AnyContent] = {
    if (!file.endsWith(".min.js") && !file.endsWith(".min.css") && Mode.Prod == Play.maybeApplication.map(_.mode).getOrElse(Mode.Dev)) {
      try {
        val newpath = controllers.WebJarAssets.locate(file.replace(".js", ".min.js").replace(".css", ".min.css"))
        return controllers.WebJarAssets.at(newpath)
      } catch {
        case nf: java.lang.IllegalArgumentException =>
      }
    }
    controllers.WebJarAssets.at(controllers.WebJarAssets.locate(file))
  }
}
