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
            Security.username -> admin.id.toString(),
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

  def redirectSSL = Action { implicit request =>
    MovedPermanently("https://" + request.host + request.uri)
  }
}

trait Formats {
  val websiteForm = Form(
    tuple(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText,
      "url" -> text,
      "email" -> optional(email)))

  val audienceForm = Form(
    tuple(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText,
      "tracking" -> optional(text),
      "paths" -> list(tuple(
        "id" -> optional(longNumber),
        "website" -> longNumber,
        "path" -> nonEmptyText,
        "include" -> boolean)),
      "websitePaths" -> list(tuple(
        "id" -> optional(longNumber),
        "allPath" -> boolean)),
      "startDate" -> optional(date),
      "endDate" -> optional(date)))

  val packageForm = Form(
    tuple(
      "id" -> optional(longNumber),
      "name" -> text,
      "startDate" -> optional(date),
      "endDate" -> optional(date),
      "count" -> number,
      "reach" -> number,
      "goal" -> number,
      "buyCpm" -> bigDecimal,
      "salesCpm" -> bigDecimal))

  val campaignForm = Form(
    tuple(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText,
      "package" -> optional(longNumber),
      "audiences" -> list(
        longNumber),
      "creatives" -> list(
        longNumber),
      "startDate" -> optional(date),
      "endDate" -> optional(date)))

  val adminForm = Form(
    tuple(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "roles" -> list(
        text),
      "url" -> optional(text),
      "streetaddress1" -> optional(text),
      "streetaddress2" -> optional(text),
      "streetaddress3" -> optional(text),
      "state" -> optional(text),
      "country" -> optional(text),
      "telephone" -> optional(text)))

  val publisherForm = Form(
    tuple(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "url" -> optional(text),
      "streetaddress1" -> optional(text),
      "streetaddress2" -> optional(text),
      "streetaddress3" -> optional(text),
      "state" -> optional(text),
      "country" -> optional(text),
      "telephone" -> optional(text)))

  val cookieForm = Form(
    tuple(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "package" -> longNumber,
      "audiences" -> list(
        longNumber),
      "creatives" -> list(
        longNumber)))

  val creativeForm = Form(
    tuple(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "package" -> longNumber,
      "audiences" -> list(
        longNumber),
      "creatives" -> list(
        longNumber)))

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
      (json \ "name").as[String]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String],
      "websiteId" -> (json \ "website").as[String],
      "urlPath" -> (json \ "url").as[String]).asJava))

    def writes(pathTarget: PathTarget) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(pathTarget.id)),
      "website" -> JsNumber(BigDecimal(pathTarget.website.id)),
      "path" -> JsString(pathTarget.urlPath),
      "variant" -> JsString(pathTarget.variant)))
  }

  implicit object WebsiteFormat extends Format[Website] {
    def reads(json: JsValue) = JsSuccess(new Website(
      (json \ "name").as[String]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String],
      "url" -> (json \ "url").as[String],
      "email" -> (json \ "email").as[String]).asJava))

    def writes(website: Website) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(website.id)),
      "url" -> JsString(website.url),
      "email" -> JsString(website.email),
      "uuid" -> JsString(website.uuid),
      "code" -> JsString(website.code(current)),
      "name" -> JsString(website.name)))
  }

  implicit object AudienceFormat extends Format[Audience] {
    def reads(json: JsValue) = JsSuccess(new Audience(
      (json \ "name").as[String]).updateFromMap(Map(
      "id" -> (json \ "id").as[String],
      "paths" -> (json \ "paths").as[Seq[PathTarget]],
      "websites" -> (json \ "websites").as[Seq[String]],
      "state" -> (json \ "state").as[String]).asJava))

    def writes(audience: Audience) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(audience.id)),
      "name" -> JsString(audience.name),
      "tracking" -> JsString(audience.tracking),
      "paths" -> Json.toJson(audience.pathTargets.asScala.filter(p => (!"*".equals(p.urlPath)))),
      "allpaths" -> JsObject(audience.pathTargets.asScala.filter(p => "*".equals(p.urlPath)).map { p =>
        p.website.id.toString -> JsString(if ("include".equals(p.variant)) "on" else "off")
      }),
      "websites" -> Json.toJson(audience.websites.asScala),
      "state" -> JsString(audience.state)))
  }

  implicit object CampaignPackageFormat extends Format[CampaignPackage] {
    def reads(json: JsValue) = JsSuccess(new CampaignPackage(
      (json \ "name").as[String]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String],
      "variant" -> (json \ "variant").as[String],
      "startDate" -> (json \ "startDate").as[String],
      "endDate" -> (json \ "endDate").as[String],
      "impressions" -> (json \ "count").as[String],
      "reach" -> (json \ "reach").as[String],
      "goal" -> (json \ "goal").as[String],
      "buyCpm" -> (json \ "buyCpm").as[String],
      "salesCpm" -> (json \ "salesCpm").as[String]).asJava))

    def writes(campaignPackage: CampaignPackage) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(campaignPackage.id)),
      "name" -> JsString(campaignPackage.name),
      "variant" -> JsString(campaignPackage.variant),
      "startDate" -> (if (campaignPackage.startDate != null) Json.toJson(campaignPackage.startDate) else JsString("")),
      "endDate" -> (if (campaignPackage.endDate != null) Json.toJson(campaignPackage.endDate) else JsString("")),
      "count" -> (if (campaignPackage.impressions != null) JsNumber(BigDecimal(campaignPackage.impressions)) else JsNumber(0)),
      "reach" -> (if (campaignPackage.reach != null) JsNumber(BigDecimal(campaignPackage.reach)) else JsNumber(0)),
      "goal" -> (if (campaignPackage.goal != null) JsNumber(BigDecimal(campaignPackage.goal)) else JsNumber(0)),
      "buyCpm" -> (if (campaignPackage.buyCpm != null) JsNumber(BigDecimal(campaignPackage.buyCpm)) else JsNumber(0)),
      "salesCpm" -> (if (campaignPackage.salesCpm != null) JsNumber(BigDecimal(campaignPackage.salesCpm)) else JsNumber(0))))
  }

  implicit object CookieFormat extends Format[models.Cookie] {
    def reads(json: JsValue) = JsSuccess(new models.Cookie(
      (json \ "name").as[String]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String]).asJava))

    def writes(cookie: models.Cookie) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(cookie.id)),
      "name" -> JsString(cookie.name),
      "content" -> JsString(cookie.content),
      "state" -> JsString(cookie.state),
      "uuid" -> JsString(cookie.uuid),
      "variant" -> JsString(cookie.variant),
      "audience" -> Json.toJson(cookie.getAudience()),
      "website" -> Json.toJson(cookie.getWebsite())))
  }

  implicit object CreativeFormat extends Format[Creative] {
    def reads(json: JsValue) = JsSuccess(new Creative(
      (json \ "name").as[String],
      (json \ "url").as[Option[String]]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String]).asJava))

    def writes(creative: Creative) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(creative.id)),
      "name" -> JsString(creative.name),
      "previewUrl" -> JsString(creative.getPreview()),
      "state" -> JsString(creative.state),
      "variant" -> JsString(creative.variant),
      "uuid" -> JsString(creative.uuid),
      "url" -> JsString(creative.getCreativeUrl())))
  }

  implicit object CampaignFormat extends Format[Campaign] {
    def reads(json: JsValue) = JsSuccess(new Campaign(
      (json \ "name").as[String]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String],
      "package" -> (json \ "package").as[CampaignPackage],
      "audiences" -> (json \ "audiences").as[Seq[String]],
      "creatives" -> (json \ "creatives").as[Seq[String]],
      "startDate" -> (json \ "startDate").as[String],
      "endDate" -> (json \ "endDate").as[String],
      "value" -> (json \ "value").as[String]).asJava))

    def writes(campaign: Campaign) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(campaign.id)),
      "name" -> JsString(campaign.name),
      "value" -> JsNumber(BigDecimal(campaign.value)),
      "startDate" -> (if (campaign.startDate != null) Json.toJson(campaign.startDate) else JsString("")),
      "endDate" -> (if (campaign.endDate != null) Json.toJson(campaign.endDate) else JsString("")),
      "package" -> Json.toJson(campaign.campaignPackage),
      "audiences" -> Json.toJson(campaign.audiences.asScala),
      "creatives" -> Json.toJson(campaign.creatives.asScala.filter(c => !"R".equals(c.state))),
      "revenue" -> JsNumber(0),
      "cost" -> JsNumber(0),
      "state" -> JsString(campaign.state)))
  }

  implicit object StringMapFormat extends Format[java.util.Map[String, String]] {
    def reads(json: JsValue) = JsSuccess(null)

    def writes(map: java.util.Map[String, String]) = JsObject(
      map.entrySet().asScala.toSeq.map(e =>
        e.getKey() -> JsString(e.getValue())))
  }

  implicit object DecimalFormat extends Format[java.math.BigDecimal] {
    def reads(json: JsValue) = JsSuccess(null)

    def writes(dec: java.math.BigDecimal) = JsNumber(dec)
  }

  implicit object StringNumberMapFormat extends Format[java.util.Map[String, java.math.BigDecimal]] {
    def reads(json: JsValue) = JsSuccess(null)

    def writes(map: java.util.Map[String, java.math.BigDecimal]) = JsObject(
      map.entrySet().asScala.toSeq.map(e =>
        e.getKey() -> Json.toJson(e.getValue())))
  }

  implicit object DatasetFormat extends Format[Dataset] {
    def reads(json: JsValue) = JsSuccess(null)

    def writes(dataset: Dataset) = JsObject(Seq(
      "values" -> Json.toJson(dataset.getContent().asScala),
      "type" -> JsString(dataset.getType()),
      "cls" -> JsString(dataset.getCls()),
      "timeframe" -> JsString(dataset.getTimeframe()),
      "name" -> JsString(dataset.getName()),
      "key" -> JsString(dataset.getName())))
  }

  implicit object PublisherFormat extends Format[Publisher] {
    def reads(json: JsValue) = JsSuccess(new Publisher(
      (json \ "name").as[String],
      (json \ "url").as[Option[String]]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String]).asJava))

    def writes(publisher: Publisher) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(publisher.id)),
      "name" -> JsString(publisher.name),
      "active" -> JsString(if (publisher.active) "true" else "false"),
      "admins" -> Json.toJson(publisher.getAdmins().asScala.map { admin =>
        JsObject(Seq(
          "id" -> JsNumber(BigDecimal(admin.id)),
          "name" -> JsString(admin.name),
          "roles" -> Json.toJson(admin.getRoles().asScala),
          "email" -> JsString(admin.email)))
      }),
      "url" -> JsString(publisher.url)))
  }

  implicit object AdminFormat extends Format[Admin] {
    def reads(json: JsValue) = JsSuccess(new Admin(
      (json \ "name").as[String],
      (json \ "email").as[String]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String]).asJava))

    def writes(admin: Admin) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(admin.id)),
      "name" -> JsString(admin.name),
      "roles" -> Json.toJson(admin.getRoles().asScala),
      "publishers" -> Json.toJson(admin.getPublishers().asScala),
      "email" -> JsString(admin.email)))
  }
}

trait Utils {
  def mapToMap(m: Map[String, Seq[String]]): java.util.Map[String, Object] = {
    m.map { v =>
      v._1 -> v._2(0)
    }
  }
}
/**
 * Provide security features
 */

trait Secured {
  val LOGINDATE = "date"

  val loginDf: DateTimeFormatter =
    DateTimeFormat.forPattern("yyyyMMddHHmmss")

  private def username(request: RequestHeader) = request.session.get(Security.username)

  /**
   * Retrieve the connected user id.
   */
  private def adminid(request: RequestHeader) = {
    request.session.get(LOGINDATE).map { d =>
      val e = loginDf.parseDateTime(d)
      if (e.plusHours(2).isAfterNow()) {
        request.session.get(Security.username)
      } else {
        None
      }
    }.getOrElse(None)
  }

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.MainController.login)

  def ActionOverHttps(f: Request[AnyContent] => Result): Action[AnyContent] = Action { request =>
    request.headers.get("x-forwarded-proto") match {
      case Some(header) => if ("https" == header) {
        f(request) match {
          case res: PlainResult => res.withHeaders(("Strict-Transport-Security", "max-age=31536000")) // or "max-age=31536000; includeSubDomains"
          case res: Result => res
        }
      } else {
        if (Play.isProd) {
          Results.Redirect("https://" + request.host + request.uri)
        } else {
          f(request)
        }
      }
      case None => f(request)
    }
  }

  def EssentialActionOverHttps(action: EssentialAction): EssentialAction = EssentialAction { request =>
    import play.api.libs.concurrent.Execution.Implicits._
    request.headers.get("x-forwarded-proto") match {
      case Some(header) => if ("https" == header) {
        action(request).map { res =>
          res match {
            case res: PlainResult => res.withHeaders(("Strict-Transport-Security", "max-age=31536000")) // or "max-age=31536000; includeSubDomains"
            case res: Result => res
          }
        }
      } else action(request).map { res =>
        if (Play.isProd) {
          Results.Redirect("https://" + request.host + request.uri)
        } else {
          res
        }
      }
      case None => action(request)
    }
  }

  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(adminid, onUnauthorized) { admin =>
    ActionOverHttps(implicit request =>
      f(admin)(request).withSession(request.session +
        (LOGINDATE -> loginDf.print({ new java.util.Date }.getTime))))
  }

  def TryAuthenticated(
    userinfo: RequestHeader => Option[String])(action: String => EssentialAction): EssentialAction = {

    EssentialAction { request =>
      userinfo(request).map { user =>
        EssentialActionOverHttps(action(user))(request)
      }.getOrElse {
        action(null)(request)
      }
    }
  }

  def CheckIfIsAuthenticated(f: => String => Request[AnyContent] => Result) = TryAuthenticated(adminid) { admin =>
    Action(implicit request =>
      f(admin)(request).withSession(request.session +
        (LOGINDATE -> loginDf.print({ new java.util.Date }.getTime))))
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

