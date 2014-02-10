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
  val domainRegex = Pattern.compile("^(http:|https:)?//[A-Za-z0-9.:@_-]*")

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

  def sendCookie(uuid: String, cookies: ArrayBuffer[String]): SimpleResult = {
    if (cookies.isEmpty) {
      cookies += "<!-- ae marker -->";
    }
    cookies += "<!-- ae://" + uuid + " -->"
    Ok("(function(){\n" +
      " function ex_scripts(e){function t(e,t){return e.nodeName&&e.nodeName.toUpperCase()===t.toUpperCase()}function n(e){var t=e[0],n=t.text||t.textContent||t.innerHTML||'',r=(document.getElementsByTagName('head')||[null])[0]||document.documentElement,i=document.createElement('script');i.type=e[1];try{i.appendChild(document.createTextNode(n))}catch(s){i.text=n}r.appendChild(i)}var r=[],i,s=e.childNodes,o,u;for(u=0;s[u];u++)o=s[u],t(o,'script')&&r.push([o,o.type]);for(u=0;r[u];u++)i=r[u],i[0].parentNode&&i[0].parentNode.removeChild(i[0]),n(i)};" +
      " var b = ((document.getElementsByTagName('head') || [null])[0] || document.getElementsByTagName('script')[0].parentNode);\n" +
      " if (b.length!=0){\n" +
      "  var d = document.createElement('div');\n" +
      "  d.style.display='none';d.innerHTML='" + (cookies.mkString("\\n")
        .replace("\n", "\\n").replace("\r", "\\n")
        .replace("'", "\\'")) + "';\n" +
      "  b.appendChild(d);\n" +
      "  ex_scripts(d);\n" +
      " }\n" +
      "})();").as("text/javascript")
  }

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
      sendCookie(uuid + "/website", cookies)
    }.getOrElse {
      models.Cookie.findByUUID(uuid).map { cookie =>
        var cookies = ArrayBuffer[String]()
        cookies += cookie.getContent
        countCookie(cookie, sub)
        sendCookie(uuid + "/cookie", cookies)
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

