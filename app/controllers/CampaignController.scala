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
      Admin.findById(adminid).map { admin =>
        Ok(
          html.campaigns(
            Campaign.findByAdmin(admin),
            admin))
      }.getOrElse(Forbidden)
  }

  def campaignJson(admin: Admin, publisherid: String): JsValue =
    Json.toJson(Campaign.findByAdmin(admin).asScala)

  def packageJson(admin: Admin, publisherid: String): JsValue =
    Json.toJson(CampaignPackage.findByAdmin(admin).asScala)

  /** Action to get the campaigns */
  def campaignList(publisherid: String) = IsAuthenticated { adminid =>
    request =>
      Admin.findById(adminid).map { admin =>
        Ok(campaignJson(admin, publisherid))
      }.getOrElse(Forbidden)
  }

  def campaignSave(publisherid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        request.body.asFormUrlEncoded.map { data =>
          data.get("id").map { ids =>
            Campaign.findById(ids(0), admin).map { campaign =>
              campaign.updateFromMap(mapToMap(data))
              val msgs = campaign.write().asScala
              Ok(JsObject(Seq(
                "data" -> Json.toJson(campaign),
                "messages" -> Json.toJson(msgs))))
            }.getOrElse(NotFound)
          }.getOrElse {
            val campaign = Campaign.fromMap(mapToMap(data))
            val publisher = Publisher.findById(publisherid, admin)
            campaign.publisher = publisher.get
            val msgs = campaign.write().asScala
            Ok(JsObject(Seq(
              "data" -> Json.toJson(campaign),
              "messages" -> Json.toJson(msgs))))
          }
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def campaignRemove(publisherid: String, campaignid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Campaign.findById(campaignid, admin).map { campaign =>
          val msgs = campaign.remove().asScala
          if (msgs.isEmpty()) {
            campaign.save()
          }
          Ok(JsObject(Seq(
            "data" -> Json.toJson(campaign),
            "messages" -> Json.toJson(msgs))))
        }.getOrElse(NotFound)
      }.getOrElse(Forbidden)
  }

  def packageList(publisherid: String) = IsAuthenticated { adminid =>
    request =>
      Admin.findById(adminid).map { admin =>
        Ok(packageJson(admin, publisherid))
      }.getOrElse(Forbidden)
  }

  def packageSave(campaignid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        request.body.asFormUrlEncoded.map { data =>
          data.get("id").map { ids =>
            CampaignPackage.findById(ids(0), admin).map { pack =>
              pack.updateFromMap(mapToMap(data))
              val msgs = pack.write().asScala
              Ok(JsObject(Seq(
                "data" -> Json.toJson(pack),
                "messages" -> Json.toJson(msgs))))
            }.getOrElse(NotFound)
          }.getOrElse {
            val pack = CampaignPackage.fromMap(mapToMap(data))
            val campaign = Campaign.findById(campaignid, admin)
            pack.campaign = campaign.get
            val msgs = pack.write().asScala
            Ok(JsObject(Seq(
              "data" -> Json.toJson(pack),
              "messages" -> Json.toJson(msgs))))
          }
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def packageRemove(campaignid: String, packageid: String) = IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        CampaignPackage.findById(packageid, admin).map { pack =>
          var msgs = Seq[Message]()
          if (pack.campaign != null && java.lang.Long.toString(pack.campaign.id).equals(campaignid)) {
            msgs = pack.remove()
            if (msgs.isEmpty()) {
              pack.save()
            }
          }
          Ok(JsObject(Seq(
            "data" -> Json.toJson(pack),
            "messages" -> Json.toJson(msgs))))
        }.getOrElse(NotFound)
      }.getOrElse(Forbidden)
  }

  def dashboard(from: String, to: String) = IsAuthenticated { adminid =>
    _ =>
      Admin.findById(adminid).map { admin =>
        Ok(Json.toJson(
          Campaign.statsByAdmin(admin, from, to).asScala))
      }.getOrElse(Forbidden)
  }

  def stats(campaignid: Long) = Action(parse.json) { implicit req =>
    Ok(Json.toJson(""))
  }
}