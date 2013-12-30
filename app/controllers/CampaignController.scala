package controllers

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import models._
import play.Logger
import play.api.libs.json._
import play.api.mvc._
import views._

object CampaignController extends Controller with Secured with Formats with Utils {
  def uploadCreative = (publisherid: String) => IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Publisher.findById(publisherid, admin).map { publisher =>
          request.body.asMultipartFormData.map { body =>
            body.file("files[]").map { file =>
              Logger.debug("uploading " + file.filename + " to " + publisher)
              Logger.debug("contentType: " + file.contentType.getOrElse("application/octet-steam"))
              Logger.debug("file: " + file.ref.file)
              //TODO: handle zip
              Creative.addUpload(publisher, file.contentType.getOrElse("application/octet-steam"),
                file.filename, file.ref.file).map { creative =>
                  Ok(Json.toJson(Seq(creative)))
                }.getOrElse(NotFound)
            }.getOrElse(NotFound)
          }.getOrElse(NotFound)
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

  def deleteCreative = (publisherid: String, creativeid: String) => IsAuthenticated { adminid =>
    implicit request =>
      Admin.findById(adminid).map { admin =>
        Publisher.findById(publisherid, admin).map { publisher =>
          Creative.findById(creativeid, admin).map { creative =>
            creative.state = "R"
            creative.update();
            Ok(Json.toJson(creative))
          }.getOrElse(Forbidden)
        }.getOrElse(Forbidden)
      }.getOrElse(Forbidden)
  }

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
            data._1.map { id =>
              Campaign.findById(id, admin).map { campaign =>
                campaign.name = data._2
                if (campaign.campaignPackage != null) {
                  if (campaign.campaignPackage.campaignPackage == null ||
                    !campaign.campaignPackage.campaignPackage.id.equals(data._3.get)) {
                    data._3.map { packid =>
                      CampaignPackage.findById(packid, admin).map { pack =>
                        campaign.campaignPackage = new CampaignPackage("")
                        campaign.campaignPackage.campaignPackage = pack
                      }
                    }
                  }
                }
                campaign.audiences.clear()
                data._4.map { audienceid =>
                  Audience.findById(audienceid, admin).map { audience =>
                    campaign.audiences.add(audience)
                  }
                }
                campaign.creatives.clear()
                data._4.map { creativeid =>
                  Creative.findById(creativeid, admin).map { creative =>
                    campaign.creatives.add(creative)
                  }
                }
                val msgs = campaign.write().asScala
                Ok(JsObject(Seq(
                  "data" -> Json.toJson(campaign),
                  "messages" -> Json.toJson(msgs))))
              }.getOrElse(NotFound)
            }.getOrElse {
              val campaign = new Campaign("")
              campaign.name = data._2
              data._3.map { packid =>
                CampaignPackage.findById(packid, admin).map { pack =>
                  campaign.campaignPackage = new CampaignPackage("")
                  campaign.campaignPackage.campaignPackage = pack
                }
              }
              campaign.audiences.clear()
              data._4.map { audienceid =>
                Audience.findById(audienceid, admin).map { audience =>
                  campaign.audiences.add(audience)
                }
              }
                campaign.creatives.clear()
                data._4.map { creativeid =>
                  Creative.findById(creativeid, admin).map { creative =>
                    campaign.creatives.add(creative)
                  }
                }
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
            data._1.map { id =>
              CampaignPackage.findById(id, admin).map { pack =>
                pack.name = data._2
                pack.variant = "custom"
                pack.startDate = data._3.getOrElse(null)
                pack.endDate = data._4.getOrElse(null)
                pack.impressions = data._5
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
              pack.impressions = data._5
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