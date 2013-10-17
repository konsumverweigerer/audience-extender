package controllers

import play.api.mvc._
import play.api._
import play.api.data._
import play.api.data.Forms._

import models._
import views._

object MainController extends Controller {
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
      user => Redirect(routes.MainController.dashboard).withSession("email" -> user._1))
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.MainController.login).withNewSession.flashing(
      "success" -> "You've been logged out")
  }

  /**
   * The index page.  This is the main entry point, seeing as this is a single page app.
   */
  def index(path: String) = Action {
    Ok(html.index())
  }

  def dashboard = Action {
    Ok(html.index())
  }

  /** The javascript router. */
  def javascriptRoutes = Action { implicit req =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("routes")(
        AdminController.getPublishers,
        AdminController.getAdmin,
        AdminController.saveAdmin)).as("text/javascript")
  }
}
