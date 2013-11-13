package controllers

import play.api.mvc._
import play.api._
import play.api.data._
import play.api.data.Forms._
import scala.collection.JavaConverters._

import models._
import services._
import views._

object MainController extends Controller with Secured {
  // -- Authentication

  val loginForm = Form(
    tuple(
      "email" -> nonEmptyText,
      "password" -> text)
      verifying ("Invalid email or password", result => result match {
        case (email, password) => (Admin.authenticate(email, password) != null)
      }))

  val contactForm = Form(
    tuple(
      "email" -> nonEmptyText,
      "name" -> text,
      "msg" -> text)
      verifying ("Could not send message", result => result match {
        case (email, name, msg) => (SendMail.sendContactMessage(email, name, msg) != null)
      }))

  val forgotPasswordForm = Form(
    tuple(
      "email" -> nonEmptyText,
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

  def policy = Action { implicit request =>
    Ok(html.policy())
  }

  def tos = Action { implicit request =>
    Ok(html.tos())
  }

  /**
   * Contact page.
   */
  def contact = Action { implicit request =>
    Ok(html.contact(contactForm))
  }

  def sendMessage = Action { implicit request =>
    contactForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.contact(formWithErrors)),
      message => Redirect(routes.MainController.contact).flashing("success" -> "Your message was sent"))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.MainController.dashboard).withSession(
        "email" -> user._1,
        "adminid" -> Admin.findByEmail(user._1).getIdString))
  }

  def forgotPassword = Action { implicit request =>
    forgotPasswordForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.MainController.login).flashing("success" -> "Password reset e-mail sent"))
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
    request.session.get("adminid").map {
      id => Ok(html.index(Admin.findById(id)))
    }.getOrElse(
      Ok(html.index(Admin.findByEmail(""))))
  }

  def dashboard = IsAuthenticated { adminid =>
    _ =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(html.dashboard(Publisher.findByAdmin(admin).asScala, admin))
      }.getOrElse(
        Ok(html.index(Admin.findByEmail(""))))
  }

  /** The javascript router. */
  def javascriptRoutes = Action { implicit req =>
    Ok(
      Routes.javascriptRouter("routes")(
        routes.javascript.PublisherController.publisherList,
        routes.javascript.AudienceController.audienceList,
        routes.javascript.AdminController.adminList)).as("text/javascript")
  }
}

/**
 * Provide security features
 */
trait Secured {

  private def username(request: RequestHeader) = request.session.get("adminid")

  /**
   * Retrieve the connected user id.
   */
  private def adminid(request: RequestHeader) = request.session.get("adminid")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.MainController.login)

  // --

  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(adminid, onUnauthorized) { admin =>
    Action(request => f(admin)(request))
  }

  /**
   * Check if the connected user is a member of this project.
   */
  def IsAdminOf(publisher: Long)(f: => String => Request[AnyContent] => Result) = IsAuthenticated { admin =>
    request =>
      if (Publisher.isAdmin(publisher, admin)) {
        f(admin)(request)
      } else {
        Results.Forbidden
      }
  }

  /**
   * Check if the connected user has role.
   */
  def HasRole(role: String)(f: => String => Request[AnyContent] => Result) = IsAuthenticated { admin =>
    request =>
      if (Admin.hasRole(admin, role)) {
        f(admin)(request)
      } else {
        Results.Forbidden
      }
  }
}

