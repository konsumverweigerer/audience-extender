package controllers

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import views._
import models._
import services._

import play.api._
import play.api.Play._
import play.api.data._
import play.api.data.Forms._

import play.api.libs.json._
import play.api.mvc._

import play.Logger

object MainController extends Controller with Secured with Formats with Utils {
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

  def sendMessage = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        contactForm.bindFromRequest.fold(
          formWithErrors => BadRequest(html.contact(formWithErrors, admin)),
          message => {
            SendMail.sendContactMessage(message._1, message._2, message._3)
            Redirect(routes.MainController.contact).flashing("success" -> "Your message was sent")
          })
      }.getOrElse(
        contactForm.bindFromRequest.fold(
          formWithErrors => BadRequest(html.contact(formWithErrors, null)),
          message => {
            SendMail.sendContactMessage(message._1, message._2, message._3)
            Redirect(routes.MainController.contact).flashing("success" -> "Your message was sent")
          }))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => {
        Logger.info("logging in: " + user._1)
        Redirect(routes.MainController.dashboard).withSession(
          Security.username -> Admin.findByEmail(user._1).id.toString())
      })
  }

  def forgotPassword = Action { implicit request =>
    forgotPasswordForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => {
        Redirect(routes.MainController.login).flashing("success" -> "Password reset e-mail sent")
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
      id => Ok(html.index(Admin.findById(id).orNull))
    }.getOrElse(
      Ok(html.index(Admin.findByEmail(""))))
  }

  def base = index("")

  def dashboardAudience = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Ok(html.dashboardaudience(Publisher.findByAdmin(admin).asScala, admin))
      }.getOrElse(
        Ok(html.index(Admin.findByEmail(""))))
  }

  def dashboardCampaign = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Ok(html.dashboardcampaign(Publisher.findByAdmin(admin).asScala, admin))
      }.getOrElse(
        Ok(html.index(Admin.findByEmail(""))))
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
        routes.javascript.CampaignController.dashboard,
        routes.javascript.AudienceController.dashboard,
        routes.javascript.PublisherController.uploadCreative,
        routes.javascript.CampaignController.campaignList,
        routes.javascript.CampaignController.campaignSave,
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
        routes.javascript.AdminController.changePublisher,
        routes.javascript.AdminController.adminList,
        routes.javascript.AdminController.adminSave)).as("text/javascript")
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

trait Formats {
  implicit object MessageFormat extends Format[Message] {
    def reads(json: JsValue) = JsSuccess(new Message(
      (json \ "title").as[String],
      (json \ "content").as[String],
      (json \ "priority").as[String]))

    def writes(message: Message) = JsObject(Seq(
      "title" -> JsString(message.title),
      "content" -> JsString(message.content),
      "priority" -> JsString(message.priority)))
  }

  implicit object PathTargetFormat extends Format[PathTarget] {
    def reads(json: JsValue) = JsSuccess(new PathTarget(
      (json \ "name").as[String]).updateFromMap(Map(
      "id" -> (json \ "id").as[String],
      "websiteId" -> (json \ "website").as[String],
      "urlPath" -> (json \ "url").as[String]
      ).asJava))

    def writes(pathTarget: PathTarget) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(pathTarget.id)),
      "website" -> JsString(pathTarget.website.id),
      "url" -> JsString(pathTarget.urlPath),
      "variant" -> JsString(pathTarget.variant)))
  }

  implicit object AudienceFormat extends Format[Audience] {
    def reads(json: JsValue) = JsSuccess(new Audience(
      (json \ "name").as[String]).updateFromMap(Map(
      "id" -> (json \ "id").as[String],
      "state" -> (json \ "state").as[String]
      ).asJava))

    def writes(audience: Audience) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(audience.id)),
      "name" -> JsString(audience.name),
      "paths" -> Json.toJson(audience.pathTargets.asScala),
      "websites" -> Json.toJson(audience.websites.asScala),
      "state" -> JsString(audience.state)))
  }

  implicit object WebsiteFormat extends Format[Website] {
    def reads(json: JsValue) = JsSuccess(new Website(
      (json \ "name").as[String]).updateFromMap(Map(
      "id" -> (json \ "id").as[String]
      ).asJava))

    def writes(website: Website) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(website.id)),
      "url" -> JsString(website.url),
      "name" -> JsString(website.name)))
  }

  implicit object CampaignFormat extends Format[Campaign] {
    def reads(json: JsValue) = JsSuccess(new Campaign(
      (json \ "name").as[String]).updateFromMap(Map(
      "id" -> (json \ "id").as[String]
      ).asJava))

    def writes(campaign: Campaign) = JsObject(Seq(
      "name" -> JsString(campaign.name)))
  }

  implicit object CampaignPackageFormat extends Format[CampaignPackage] {
    def reads(json: JsValue) = JsSuccess(new CampaignPackage(
      (json \ "name").as[String]).updateFromMap(Map(
      "id" -> (json \ "id").as[String]).asJava))

    def writes(campaignPackage: CampaignPackage) = JsObject(Seq(
      "name" -> JsString(campaignPackage.name)))
  }

  implicit object StringMapFormat extends Format[java.util.Map[String, String]] {
    def reads(json: JsValue) = JsSuccess(null)

    def writes(map: java.util.Map[String, String]) = JsObject(
      map.entrySet().asScala.toSeq.map(e =>
        e.getKey() -> JsString(e.getValue())))
  }

  implicit object DatasetFormat extends Format[Dataset] {
    def reads(json: JsValue) = JsSuccess(null)

    def writes(dataset: Dataset) = JsObject(Seq(
      "values" -> Json.toJson(dataset.getValues().asScala.toSeq),
      "type" -> JsString(dataset.getType()),
      "name" -> JsString(dataset.getName())))
  }

  implicit object PublisherFormat extends Format[Publisher] {
    def reads(json: JsValue) = JsSuccess(new Publisher(
      (json \ "name").as[String],
      (json \ "url").as[Option[String]]).updateFromMap(Map(
      "id" -> (json \ "id").as[String]).asJava))

    def writes(publisher: Publisher) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(publisher.id)),
      "name" -> JsString(publisher.name),
      "active" -> JsString(if (publisher.active) "true" else "false"),
      "url" -> JsString(publisher.url)))
  }

  implicit object CreativeFormat extends Format[Creative] {
    def reads(json: JsValue) = JsSuccess(new Creative(
      (json \ "name").as[String],
      (json \ "url").as[Option[String]]).updateFromMap(Map(
      "id" -> (json \ "id").as[String]).asJava))

    def writes(creative: Creative) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(creative.id)),
      "name" -> JsString(creative.name),
      "preview" -> JsString(creative.getPreview()),
      "uuid" -> JsString(creative.uuid),
      "url" -> JsString(creative.url)))
  }

  implicit object AdminFormat extends Format[Admin] {
    def reads(json: JsValue) = JsSuccess(new Admin(
      (json \ "name").as[String],
      (json \ "email").as[String]).updateFromMap(Map(
      "id" -> (json \ "id").as[String]).asJava))

    def writes(admin: Admin) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(admin.id)),
      "name" -> JsString(admin.name),
      "roles" -> Json.toJson(admin.getRoles().asScala),
      "publishers" -> Json.toJson(admin.publishers.asScala),
      "email" -> JsString(admin.email)))
  }
}

trait Utils {
  def mapToMap(m: Map[String, Seq[String]]): java.util.Map[String, String] = {
    m.map { v =>
      v._1 -> v._2(0)
    }
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

