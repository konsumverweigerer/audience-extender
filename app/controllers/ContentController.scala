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

object ContentController extends Controller with Utils {
  def checkPaths(paths: Seq[PathTarget]): Seq[Long] = {
    val inc: Seq[Long] = Seq()
    val exc: Seq[Long] = Seq()
    val audiences: Seq[Long] = Seq()
    audiences
  }

  def cookie = (uuid: String, sub: String) => Action { implicit request =>
    Website.findByUUID(uuid).map { website =>
      val cookies: Seq[String] = Seq()
      checkPaths(website.pathTargets.asScala).map { audienceid =>
        models.Cookie.findByWebsite(website.id, audienceid).asScala.foreach { cookie =>
          if ("A".equals(cookie.state) && "code".equals(cookie.variant)) {
            cookies.add(cookie.content)
          }
        }
      }
      Ok("function(){document.write('" + (cookies.mkString("\\n")
        .replace("\n", "\\n").replace("\r", "\\n")
        .replace("'", "\\'")) + "');\n}()").as("text/javascript")
    }.getOrElse(NotFound.as("text/javascript"))
  }

  def creative = (uuid: String) => Action { implicit request =>
    Creative.findByUUID(uuid).map { creative =>
      Ok(creative.data).as(creative.variant)
    }.getOrElse(NotFound)
  }
}

