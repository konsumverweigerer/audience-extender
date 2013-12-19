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
    val inc: Seq[Long] = ArrayBuffer()
    val exc: Seq[Long] = ArrayBuffer()
    val audiences: HashSet[Long] = HashSet[Long]()
    paths.filter(p => "*".equals(p.urlPath)).foreach { p =>
      if ("include".equals(p.variant)) {
        inc.add(p.audience.id)
      } else if ("exclude".equals(p.variant)) {
        exc.add(p.audience.id)
      }
    }
    audiences.addAll(inc);
    paths.filter(p => !"*".equals(p.urlPath)).foreach { p =>
      val id = p.audience.id
      val pat = Pattern.compile(p.urlPath)
      if (!pat.matcher(reqPath).find()) {
        Logger.debug("pattern " + p.urlPath + " not matching " + reqPath)
      } else if ("include".equals(p.variant)) {
        if (exc.contains(id)) {
          audiences.add(id)
        }
      } else if ("exclude".equals(p.variant)) {
        if (inc.contains(id)) {
          audiences.remove(id)
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
      StatsHandler.countcookie(cookie.id, sub)
    }
  }

  def countCreative(creative: models.Creative) = {
    future {
      StatsHandler.countcreative(creative.id)
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
      checkPaths(extractPath(request.headers, request.queryString), website.pathTargets.asScala).map { audienceid =>
        models.Cookie.findByWebsite(website.id, audienceid).asScala.foreach { cookie =>
          if ("A".equals(cookie.state) && "code".equals(cookie.variant)) {
            cookies.add(cookie.content)
            countCookie(cookie, sub)
          }
        }
        //TODO: add 3rd party tracking
      }
      sendCookie(cookies)
    }.getOrElse {
      models.Cookie.findByUUID(uuid).map { cookie =>
        var cookies = ArrayBuffer[String]()
        cookies.add(cookie.content)
        countCookie(cookie, sub)
        sendCookie(cookies)
      }.getOrElse(NotFound.as("text/javascript"))
    }
  }

  def creativeContent = (uuid: String, t: String) => Action { implicit request =>
    Creative.findByUUID(uuid).map { creative =>
      if ("external".equals(creative.variant)) {
        Redirect(creative.url)
      } else if (creative.data != null && "preview".equals(t)) {
        Ok(Image(creative.data).fit(176, 74).write(com.sksamuel.scrimage.Format.PNG)).as("image/png")
      } else if (creative.data != null) {
        Ok(creative.data).as(creative.variant)
      } else {
        Ok("").as("application/octet-steam")
      }
    }.getOrElse(NotFound)
  }

  def creative = (uuid: String) => Action { implicit request =>
    Creative.findByUUID(uuid).map { creative =>
      countCreative(creative)
      Ok(creative.data).as(creative.variant)
    }.getOrElse(NotFound)
  }
}

