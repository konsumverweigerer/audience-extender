package controllers

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable.{ ArrayBuffer, HashSet }
import scala.concurrent._

import models._
import views._
import services._

import play.api._
import play.api.Play._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

import com.sksamuel.scrimage._

import play.Logger

import java.util.regex.Pattern

object ContentController extends Controller with Utils {
  val domainRegex = Pattern.compile("^(http:|https:)?//[A-Za-z0-9.:@_-]")

  def checkPaths(reqPath: String, paths: Seq[PathTarget]): HashSet[Long] = {
    val inc = ArrayBuffer[Long]()
    val exc = ArrayBuffer[Long]()
    val audiences = HashSet[Long]()
    paths.filter(p => "*".equals(p.getUrlPath)).foreach { p =>
      if ("include".equals(p.getVariant)) {
        inc += p.getAudience.getId
      } else if ("exclude".equals(p.getVariant)) {
        exc += p.getAudience.getId
      }
    }
    audiences ++= inc;
    paths.filter(p => !"*".equals(p.getUrlPath)).foreach { p =>
      val id = p.getAudience.getId
      val pat = Pattern.compile(p.getUrlPath)
      if (!pat.matcher(reqPath).find()) {
        Logger.debug("pattern " + p.getUrlPath + " not matching " + reqPath)
      } else if ("include".equals(p.getVariant)) {
        if (exc.contains(id)) {
          audiences += id
        }
      } else if ("exclude".equals(p.getVariant)) {
        if (inc.contains(id)) {
          audiences += id
        }
      }
    }
    audiences
  }

  def extractPath(headers: Headers, query: Map[String, Seq[String]]): String = {
    var path = ""
    headers.get(REFERER).map(p => path = p).getOrElse {
      query.filter(p => "l".equals(p._1)).map { v =>
        v._2.map { l =>
          path = l
        }
      }
    }
    domainRegex.matcher(path).replaceAll("")
  }

  def countCookie(cookie: models.Cookie, sub: String) = {
    future {
      StatsHandler.countcookie(cookie.getId, sub)
    }
  }

  def countCreative(creative: models.Creative) = {
    future {
      StatsHandler.countcreative(creative.getId)
    }
  }

  def sendCookie(cookies: Seq[String]): SimpleResult =
    Ok("(function(){\nvar b = document.getElementsByTagName('body');\n" +
      "if (b.length!=0){var d = document.createElement('div');\n" +
      "d.style.display='none';d.innerHTML='" + (cookies.mkString("\\n")
        .replace("\n", "\\n").replace("\r", "\\n")
        .replace("'", "\\'")) + "';\nb[0].appendChild(d);\n}})();").as("text/javascript")

  def cookie = (uuid: String, sub: String) => Action { implicit request =>
    Website.findByUUID(uuid).map { website =>
      var cookies = ArrayBuffer[String]()
      checkPaths(extractPath(request.headers, request.queryString), website.getPathTargets.asScala).map { audienceid =>
        models.Cookie.findByWebsite(website.getId, audienceid).asScala.foreach { cookie =>
          if ("A".equals(cookie.getState) && "code".equals(cookie.getVariant)) {
            cookies += cookie.getContent
            countCookie(cookie, sub)
          }
        }
        var tracking = ArrayBuffer[Long]()
        website.getPathTargets.map { target =>
          val audience = target.getAudience
          if (audienceid.equals(audience.getId)) {
            if (!tracking.contains(audience.getId)) {
              if (audience.getTracking != null) {
                cookies += audience.getTracking
              }
              tracking += audience.getId
            }
          }
        }
      }
      sendCookie(cookies)
    }.getOrElse {
      models.Cookie.findByUUID(uuid).map { cookie =>
        var cookies = ArrayBuffer[String]()
        cookies += cookie.getContent
        countCookie(cookie, sub)
        sendCookie(cookies)
      }.getOrElse(NotFound.as("text/javascript"))
    }
  }

  def creativeContent = (uuid: String, t: String) => Action { implicit request =>
    Creative.findByUUID(uuid).map { creative =>
      if ("external".equals(creative.getVariant)) {
        Redirect(creative.getUrl)
      } else if (creative.getData != null && "preview".equals(t)) {
        Ok(Image(creative.getData).fit(176, 74).write(com.sksamuel.scrimage.Format.PNG)).as("image/png")
      } else if (creative.getData != null) {
        Ok(creative.getData).as(creative.getVariant)
      } else {
        Ok("").as("application/octet-steam")
      }
    }.getOrElse(NotFound)
  }

  def websiteContent = (wid: String, t: String) => Action { implicit request =>
    NotFound
  }

  def campaignPackageContent = (cid: String, t: String) => Action { implicit request =>
    NotFound
  }

  def creative = (uuid: String) => Action { implicit request =>
    Creative.findByUUID(uuid).map { creative =>
      countCreative(creative)
      Ok(creative.getData).as(creative.getVariant)
    }.getOrElse(NotFound)
  }
}

