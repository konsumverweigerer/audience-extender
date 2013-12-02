package controllers

import scala.collection.JavaConverters.asScalaBufferConverter

import models._
import services._

import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import views.html

import play.Logger

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

  def policy = CheckIfIsAuthenticated { adminid =>
    implicit request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(html.policy(admin))
      }.getOrElse(
        Ok(html.policy(null)))
  }

  def tos = CheckIfIsAuthenticated { adminid =>
    implicit request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(html.tos(admin))
      }.getOrElse(
        Ok(html.tos(null)))
  }

  /**
   * Contact page.
   */
  def contact = CheckIfIsAuthenticated { adminid =>
    implicit request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(html.contact(contactForm, admin))
      }.getOrElse(
        Ok(html.contact(contactForm, null)))
  }

  def sendMessage = IsAuthenticated { adminid =>
    implicit request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        contactForm.bindFromRequest.fold(
          formWithErrors => BadRequest(html.contact(formWithErrors, admin)),
          message => Redirect(routes.MainController.contact).flashing("success" -> "Your message was sent"))
      }.getOrElse(
        contactForm.bindFromRequest.fold(
          formWithErrors => BadRequest(html.contact(formWithErrors, null)),
          message => Redirect(routes.MainController.contact).flashing("success" -> "Your message was sent")))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.MainController.dashboard).withSession(
        Security.username -> Admin.findByEmail(user._1).getIdString))
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
    request.session.get(Security.username).map {
      id => Ok(html.index(Admin.findById(id)))
    }.getOrElse(
      Ok(html.index(Admin.findByEmail(""))))
  }

  def base = index("")

  def dashboardAudience = IsAuthenticated { adminid =>
    implicit request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(html.dashboardaudience(Publisher.findByAdmin(admin).asScala, admin))
      }.getOrElse(
        Ok(html.index(Admin.findByEmail(""))))
  }

  def dashboardCampaign = IsAuthenticated { adminid =>
    implicit request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(html.dashboardcampaign(Publisher.findByAdmin(admin).asScala, admin))
      }.getOrElse(
        Ok(html.index(Admin.findByEmail(""))))
  }

  def dashboard = dashboardAudience

  def dashboardSelect(t: String = "audience") = IsAuthenticated { adminid =>
    implicit request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        if ("campaign".equals(t)) {
          Ok(html.dashboardcampaign(Publisher.findByAdmin(admin).asScala, admin))
        } else {
          Ok(html.dashboardaudience(Publisher.findByAdmin(admin).asScala, admin))
        }
      }.getOrElse(
        Ok(html.index(Admin.findByEmail(""))))
  }

  /** The javascript router. */
  def javascriptRoutes = Action { implicit req =>
    Ok(
      Routes.javascriptRouter("routes")(
        routes.javascript.PublisherController.publisherList,
        routes.javascript.PublisherController.dashboard,
        routes.javascript.PublisherController.stats,
        routes.javascript.CampaignController.campaignList,
        routes.javascript.CampaignController.dashboard,
        routes.javascript.AudienceController.audienceList,
        routes.javascript.AdminController.adminList)).as("text/javascript")
  }

  def resourceNameAt(path: String, file: String): Option[String] = {
    val decodedFile = play.utils.UriEncoding.decodePath(file, "utf-8")
    val resourceName = Option(path + "/" + decodedFile).map(name => if (name.startsWith("/")) name else ("/" + name)).get
    if (new java.io.File(resourceName).isDirectory || !new java.io.File(resourceName).getCanonicalPath.startsWith(new java.io.File(path).getCanonicalPath)) {
      None
    } else {
      Some(resourceName)
    }
  }

  /** TODO: handle NotFound **/
  def minAssetsAt(path: String, file: String) = {
    import Play.current
    Logger.info("get minified " + path + " / " + file)
    if (file.endsWith(".min.js")) {
      controllers.Assets.at(path, file)
    } else {
      val newfile = file.replace(".js", ".min.js")
      Logger.info("really get minified " + path + " / " + newfile)
      resourceNameAt(path, newfile).map(resourceName => {
        Play.resource(resourceName).map(resource => {
          Logger.info("getting minified " + path + " / " + newfile + " at " + resourceName)
          controllers.Assets.at(path, newfile)
        }).getOrElse(Option())
      }).getOrElse(resourceNameAt(path, file).map(resourceName => {
        Play.resource(resourceName).map(resource => {
          Logger.info("getting fallback " + path + " / " + file + " at " + resourceName)
          controllers.Assets.at(path, file)
        }).getOrElse(
          controllers.Assets.at(path, file))
      }).getOrElse(
        controllers.Assets.at(path, file)))
    }
  }

  def minProdWebJarAssetsAt(file: String): Action[AnyContent] = {
    if (!file.endsWith(".min.js") && Mode.Prod == Play.maybeApplication.map(_.mode).getOrElse(Mode.Dev)) {
      try {
        val newpath = controllers.WebJarAssets.locate(file.replace(".js", ".min.js"))
        return controllers.WebJarAssets.at(newpath)
      } catch {
        case nf: java.lang.IllegalArgumentException =>
      }
    }
    controllers.WebJarAssets.at(controllers.WebJarAssets.locate(file))
  }

  /** TODO: handle NotFound **/
  def minProdAssetsAt(path: String, file: String): Action[AnyContent] = {
    Logger.info("get prod minified " + path + " / " + file)
    if (file.endsWith(".js")) {
      if (!file.endsWith(".min.js") && Mode.Prod == Play.maybeApplication.map(_.mode).getOrElse(Mode.Dev)) {
        return controllers.Assets.at(path, file.replace(".js", ".min.js"))
      }
    } else if (file.endsWith(".css")) {
      if (!file.endsWith(".min.css") && Mode.Prod == Play.maybeApplication.map(_.mode).getOrElse(Mode.Dev)) {
        return controllers.Assets.at(path, file.replace(".css", ".min.css"))
      }
    }
    controllers.Assets.at(path, file)
  }
}

/**
 * Provide security features
 */
trait Secured {

  private def username(request: RequestHeader) = request.session.get(Security.username)

  /**
   * Retrieve the connected user id.
   */
  private def adminid(request: RequestHeader) = request.session.get(Security.username)

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

  def TryAuthenticated(
    userinfo: RequestHeader => Option[String])(action: String => EssentialAction): EssentialAction = {

    EssentialAction { request =>
      userinfo(request).map { user =>
        action(user)(request)
      }.getOrElse {
        action(null)(request)
      }
    }
  }

  def CheckIfIsAuthenticated(f: => String => Request[AnyContent] => Result) = TryAuthenticated(adminid) { admin =>
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

