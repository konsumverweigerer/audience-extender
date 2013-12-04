package controllers

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import models._
import views._

import play.api._
import play.api.Play._
import play.api.data._
import play.api.data.Forms._

import play.api.libs.json._
import play.api.mvc._

import play.Logger

object CampaignController extends Controller with Secured with Formats with Utils {
  def campaigns = IsAuthenticated { adminid =>
    _ =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(
          html.campaigns(
            Campaign.findByAdmin(admin).asScala,
            admin))
      }.getOrElse(Forbidden)
  }

  def campaignJson(admin: Admin): JsValue =
    Json.toJson(Campaign.findByAdmin(admin).asScala)

  /** Action to get the campaigns */
  def campaignList(publisherid: String) = IsAuthenticated { adminid =>
    request =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(campaignJson(admin))
      }.getOrElse(Forbidden)
  }

  def dashboard(from: String, to: String) = IsAuthenticated { adminid =>
    _ =>
      Option[Admin](Admin.findById(adminid)).map { admin =>
        Ok(Json.toJson(
          Campaign.statsByAdmin(admin, from, to).asScala))
      }.getOrElse(Forbidden)
  }

  def stats(campaignid: Long) = Action(parse.json) { implicit req =>
    Ok(Json.toJson(""))
  }
}