package controllers

import play.api.mvc._
import play.api._
import play.api.data._
import play.api.data.Forms._

import models._
import views._

object MainController extends Controller with Secured {
  // -- Authentication

  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text) verifying ("Invalid email or password", result => result match {
        case (email, password) => (Admin.authenticate(email, password) != null)
      }))

  /**
   * Login page.
   */
  def login = Action { implicit request =>
    Ok(html.login(loginForm))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.MainController.dashboard).withSession("email" -> user._1, "adminid" -> Admin.findByEmail(user._1).getIdString))
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

  def dashboard = Action { implicit request =>
    request.session.get("adminid").map {
      id => Ok(html.index(Admin.findById(id)))
    }.getOrElse(
      Ok(html.index(Admin.findByEmail(""))))
  }

  /** The javascript router. */
  def javascriptRoutes = Action { implicit req =>
    Ok(
      Routes.javascriptRouter("routes")(
        routes.javascript.AdminController.getPublishers,
        routes.javascript.AdminController.getAdmin,
        routes.javascript.AdminController.saveAdmin)).as("text/javascript")
  }
}

/**
 * Provide security features
 */
trait Secured {

  /**
   * Retrieve the connected user email.
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
}

