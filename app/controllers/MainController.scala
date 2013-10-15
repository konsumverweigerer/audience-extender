package controllers

import play.api.mvc._
import play.api.Routes
import play.api.libs.EventSource

object MainController extends Controller {

  /**
   * The index page.  This is the main entry point, seeing as this is a single page app.
   */
  def index(path: String) = Action {
    Ok(views.html.index())
  }

  /** The javascript router. */
  def router = Action { implicit req =>
    Ok(
      Routes.javascriptRouter("routes")(
        routes.javascript.AdminController.getPublishers,
        routes.javascript.AdminController.getAdmin,
        routes.javascript.AdminController.saveAdmin
      )
    ).as("text/javascript")
  }
}
