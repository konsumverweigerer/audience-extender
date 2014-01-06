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
import java.util.Date

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
      "endDate" -> optional(date),
      "variant" -> text))

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
      "title" -> JsString(message.getTitle),
      "content" -> JsString(message.getContent),
      "priority" -> JsString(message.getPriority)))
  }

  implicit object PathTargetFormat extends Format[PathTarget] {
    def reads(json: JsValue) = JsSuccess(new PathTarget(
      (json \ "name").as[String]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String],
      "websiteId" -> (json \ "website").as[String],
      "urlPath" -> (json \ "url").as[String]).asJava))

    def writes(pathTarget: PathTarget) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(pathTarget.getId)),
      "website" -> JsNumber(BigDecimal(pathTarget.getWebsite.getId)),
      "path" -> JsString(pathTarget.getUrlPath),
      "active" -> JsString(if ("include".equals(pathTarget.getVariant)) "on" else "off"),
      "variant" -> JsString(pathTarget.getVariant)))
  }

  implicit object WebsiteFormat extends Format[Website] {
    def reads(json: JsValue) = JsSuccess(new Website(
      (json \ "name").as[String]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String],
      "url" -> (json \ "url").as[String],
      "email" -> (json \ "email").as[String]).asJava))

    def writes(website: Website) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(website.getId)),
      "url" -> JsString(website.getUrl),
      "email" -> JsString(website.getEmail),
      "uuid" -> JsString(website.getUuid),
      "codesimple" -> JsString(website.code(current)),
      "code" -> JsString(website.extendedCode(current)),
      "name" -> JsString(website.getName)))
  }

  implicit object AudienceFormat extends Format[Audience] {
    def reads(json: JsValue) = JsSuccess(new Audience(
      (json \ "name").as[String]).updateFromMap(Map(
      "id" -> (json \ "id").as[String],
      "paths" -> (json \ "paths").as[Seq[PathTarget]],
      "websites" -> (json \ "websites").as[Seq[String]],
      "state" -> (json \ "state").as[String]).asJava))

    def writes(audience: Audience) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(audience.getId)),
      "name" -> JsString(audience.getName),
      "tracking" -> JsString(audience.getTracking),
      "paths" -> Json.toJson(audience.getPathTargets.asScala.filter(p => (!"*".equals(p.getUrlPath)))),
      "allpaths" -> JsObject(audience.getPathTargets.asScala.filter(p => "*".equals(p.getUrlPath)).map { p =>
        p.getWebsite.getId.toString -> JsString(if ("include".equals(p.getVariant)) "on" else "off")
      }),
      "websites" -> Json.toJson(audience.getWebsites.asScala),
      "state" -> JsString(audience.getState)))
  }

  implicit object CampaignPackageFormat extends Format[CampaignPackage] {
    def reads(json: JsValue) = JsSuccess(new CampaignPackage(
      (json \ "name").as[String]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String],
      "variant" -> (json \ "variant").as[String],
      "startDate" -> (json \ "startDate").as[Date],
      "endDate" -> (json \ "endDate").as[Date],
      "impressions" -> (json \ "count").as[String],
      "reach" -> (json \ "reach").as[String],
      "goal" -> (json \ "goal").as[String],
      "buyCpm" -> (json \ "buyCpm").as[String],
      "salesCpm" -> (json \ "salesCpm").as[String]).asJava))

    def writes(campaignPackage: CampaignPackage) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(campaignPackage.getId)),
      "name" -> JsString(campaignPackage.getName),
      "variant" -> JsString(campaignPackage.getVariant),
      "startDate" -> (if (campaignPackage.getStartDate != null) Json.toJson(campaignPackage.getStartDate) else JsString("")),
      "endDate" -> (if (campaignPackage.getEndDate != null) Json.toJson(campaignPackage.getEndDate) else JsString("")),
      "count" -> (if (campaignPackage.getImpressions != null) JsNumber(BigDecimal(campaignPackage.getImpressions)) else JsNumber(0)),
      "reach" -> (if (campaignPackage.getReach != null) JsNumber(BigDecimal(campaignPackage.getReach)) else JsNumber(0)),
      "goal" -> (if (campaignPackage.getGoal != null) JsNumber(BigDecimal(campaignPackage.getGoal)) else JsNumber(0)),
      "buyCpm" -> (if (campaignPackage.getBuyCpm != null) JsNumber(BigDecimal(campaignPackage.getBuyCpm)) else JsNumber(0)),
      "salesCpm" -> (if (campaignPackage.getSalesCpm != null) JsNumber(BigDecimal(campaignPackage.getSalesCpm)) else JsNumber(0))))
  }

  implicit object CookieFormat extends Format[models.Cookie] {
    def reads(json: JsValue) = JsSuccess(new models.Cookie(
      (json \ "name").as[String]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String]).asJava))

    def writes(cookie: models.Cookie) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(cookie.getId)),
      "name" -> JsString(cookie.getName),
      "content" -> JsString(cookie.getContent),
      "state" -> JsString(cookie.getState),
      "uuid" -> JsString(cookie.getUuid),
      "variant" -> JsString(cookie.getVariant),
      "audience" -> Json.toJson(cookie.getAudience),
      "website" -> Json.toJson(cookie.getWebsite)))
  }

  implicit object CreativeFormat extends Format[Creative] {
    def reads(json: JsValue) = JsSuccess(new Creative(
      (json \ "name").as[String],
      (json \ "url").as[Option[String]]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String]).asJava))

    def writes(creative: Creative) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(creative.getId)),
      "name" -> JsString(creative.getName),
      "previewUrl" -> JsString(creative.getPreview),
      "state" -> JsString(creative.getState),
      "variant" -> JsString(creative.getVariant),
      "uuid" -> JsString(creative.getUuid),
      "url" -> JsString(creative.getCreativeUrl)))
  }

  implicit object CampaignFormat extends Format[Campaign] {
    def reads(json: JsValue) = JsSuccess(new Campaign(
      (json \ "name").as[String]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String],
      "package" -> (json \ "package").as[CampaignPackage],
      "audiences" -> (json \ "audiences").as[Seq[String]],
      "creatives" -> (json \ "creatives").as[Seq[String]],
      "startDate" -> (json \ "startDate").as[Date],
      "endDate" -> (json \ "endDate").as[Date],
      "variant" -> (json \ "variant").as[String],
      "value" -> (json \ "value").as[String]).asJava))

    def writes(campaign: Campaign) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(campaign.getId)),
      "name" -> JsString(campaign.getName),
      "value" -> JsNumber(BigDecimal(campaign.getValue)),
      "startDate" -> (if (campaign.getStartDate != null) Json.toJson(campaign.getStartDate) else JsString("")),
      "endDate" -> (if (campaign.getEndDate != null) Json.toJson(campaign.getEndDate) else JsString("")),
      "package" -> Json.toJson(campaign.getCampaignPackage),
      "audiences" -> Json.toJson(campaign.getAudiences.asScala),
      "creatives" -> Json.toJson(campaign.getCreatives.asScala.filter(c => !"R".equals(c.getState))),
      "revenue" -> JsNumber(0),
      "cost" -> JsNumber(0),
      "variant" -> JsString(campaign.getVariant),
      "state" -> JsString(campaign.getState)))
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
      "id" -> JsNumber(BigDecimal(publisher.getId)),
      "name" -> JsString(publisher.getName),
      "active" -> JsString(if (publisher.isActive) "true" else "false"),
      "admins" -> Json.toJson(publisher.getAdmins().asScala.map { admin =>
        JsObject(Seq(
          "id" -> JsNumber(BigDecimal(admin.getId)),
          "name" -> JsString(admin.getName),
          "roles" -> Json.toJson(admin.getRoles().asScala),
          "email" -> JsString(admin.getEmail)))
      }),
      "url" -> JsString(publisher.getUrl)))
  }

  implicit object AdminFormat extends Format[Admin] {
    def reads(json: JsValue) = JsSuccess(new Admin(
      (json \ "name").as[String],
      (json \ "email").as[String]).updateFromMap(Map[String, Object](
      "id" -> (json \ "id").as[String]).asJava))

    def writes(admin: Admin) = JsObject(Seq(
      "id" -> JsNumber(BigDecimal(admin.getId)),
      "name" -> JsString(admin.getName),
      "roles" -> Json.toJson(admin.getRoles().asScala),
      "publishers" -> Json.toJson(admin.getPublishers().asScala),
      "email" -> JsString(admin.getEmail)))
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
  private def onUnauthorized(request: RequestHeader) = {
    val res = Results.Redirect(routes.MainController.login)
    if (request.session.get(Security.username).nonEmpty) {
      res.flashing("loginmsg" -> "Your login timed out")
    } else {
      res
    }
  }

  def ActionOverHttps(f: Request[AnyContent] => Result): Action[AnyContent] = Action { request =>
    request.headers.get("x-forwarded-proto") match {
      case Some(header) => if ("https" == header) {
        f(request) match {
          case res: SimpleResult => res.withHeaders(("Strict-Transport-Security", "max-age=31536000")) // or "max-age=31536000; includeSubDomains"
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
          res.withHeaders(("Strict-Transport-Security", "max-age=31536000")) // or "max-age=31536000; includeSubDomains"
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