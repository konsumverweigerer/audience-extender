package controllers

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.JavaConverters.asScalaBufferConverter

import models.Admin
import models.Campaign
import models.CampaignPackage
import models.Message
import models.Publisher
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import views.html

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
        campaignForm.bindFromRequest.fold(
          errors => {
            val msgs = Seq(new Message("error", errors.globalError.map(e => e.message).getOrElse("error"), "error"))
            BadRequest(JsObject(Seq(
              "data" -> Json.toJson(Map[String, String]()),
              "messages" -> Json.toJson(msgs))))
          },
          data =>
            Some(data._1).map { id =>
              Campaign.findById(id.getOrElse(-1L), admin).map { campaign =>
                //TODO: fill from form
                val msgs = campaign.write().asScala
                Ok(JsObject(Seq(
                  "data" -> Json.toJson(campaign),
                  "messages" -> Json.toJson(msgs))))
              }.getOrElse(NotFound)
            }.getOrElse {
              val campaign = new Campaign("")
              //TODO: fill from form
              val publisher = Publisher.findById(publisherid, admin)
              campaign.publisher = publisher.get
              val msgs = campaign.write().asScala
              Ok(JsObject(Seq(
                "data" -> Json.toJson(campaign),
                "messages" -> Json.toJson(msgs))))
            })
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
        packageForm.bindFromRequest.fold(
          errors => {
            val msgs = Seq(new Message("error", errors.globalError.map(e => e.message).getOrElse("error"), "error"))
            BadRequest(JsObject(Seq(
              "data" -> Json.toJson(Map[String, String]()),
              "messages" -> Json.toJson(msgs))))
          },
          data =>
            Some(data._1).map { id =>
              CampaignPackage.findById(id.getOrElse(-1L), admin).map { pack =>
                pack.name = data._2
                pack.variant = "custom"
                pack.startDate = data._3.getOrElse(null)
                pack.endDate = data._4.getOrElse(null)
                pack.count = data._5
                pack.reach = data._6
                pack.goal = data._7
                if (data._8 != null) {
                  pack.buyCpm = new java.math.BigDecimal(data._8.toString)
                }
                if (data._9 != null) {
                  pack.salesCpm = new java.math.BigDecimal(data._9.toString)
                }
                val msgs = pack.write().asScala
                Ok(JsObject(Seq(
                  "data" -> Json.toJson(pack),
                  "messages" -> Json.toJson(msgs))))
              }.getOrElse(NotFound)
            }.getOrElse {
              val pack = new CampaignPackage("")
              pack.name = data._2
              pack.variant = "custom"
              pack.startDate = data._3.getOrElse(null)
              pack.endDate = data._4.getOrElse(null)
              pack.count = data._5
              pack.reach = data._6
              pack.goal = data._7
              if (data._8 != null) {
                pack.buyCpm = new java.math.BigDecimal(data._8.toString)
              }
              if (data._9 != null) {
                pack.salesCpm = new java.math.BigDecimal(data._9.toString)
              }
              val campaign = Campaign.findById(campaignid, admin)
              pack.campaign = campaign.get
              val msgs = pack.write().asScala
              Ok(JsObject(Seq(
                "data" -> Json.toJson(pack),
                "messages" -> Json.toJson(msgs))))
            })
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

  def dashboard(from: Long, to: Long) = IsAuthenticated { adminid =>
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