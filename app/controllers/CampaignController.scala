package controllers

import play.api.libs.json._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import scala.collection.JavaConverters._

import models._
import views._

object CampaignController extends Controller with Secured {
  implicit object CampaignFormat extends Format[Campaign] {
    def reads(json: JsValue) = JsSuccess(new Campaign(
      (json \ "name").as[String]))

    def writes(campaign: Campaign) = JsObject(Seq(
      "name" -> JsString(campaign.name)))
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

  def campaigns = IsAuthenticated { adminid =>
    _ =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(
          html.campaigns(
            Campaign.findByAdmin(admin).asScala,
            admin))
      }.getOrElse(Forbidden)
  }

  def campaignJson(admin: Admin, state: String, query: String): JsValue =
    Json.toJson(Campaign.findByAdmin(admin).asScala)

  /** Action to get the campaigns */
  def campaignList(state: String, query: String, page: Int, perPage: Int) = IsAuthenticated { adminid =>
    _ =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(campaignJson(admin,state, query))
      }.getOrElse(Forbidden)
  }

  def dashboard(from: String, to: String, state: String, query: String) = IsAuthenticated { adminid =>
    _ =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(Json.toJson(
          Campaign.statsByAdmin(admin, from, to, state, query).asScala))
      }.getOrElse(Forbidden)
  }

  def stats(campaignid: Long) = Action(parse.json) { implicit req =>
    Ok(Json.toJson(""))
  }
}