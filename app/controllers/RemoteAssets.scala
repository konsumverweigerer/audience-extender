package controllers

import play.api.mvc._
import play.api.Play
import play.api.Play.current
import org.joda.time.format.{ DateTimeFormat, DateTimeFormatter }
import org.joda.time.DateTimeZone
import scala.Some
import scala.concurrent.Future

object RemoteAssets extends Controller {
  private val timeZoneCode = "GMT"

  private val df: DateTimeFormatter =
    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss '" + timeZoneCode + "'").withLocale(java.util.Locale.ENGLISH).withZone(DateTimeZone.forID(timeZoneCode))

  type ResultWithHeaders = SimpleResult { def withHeaders(headers: (String, String)*): SimpleResult }

  def getAsset(path: String, file: String): Action[AnyContent] = Action.async { request =>
    val action = Assets.at(path, file)
    implicit val ex = action.executionContext
    action.apply(request).map { r => r.withHeaders(DATE -> df.print({ new java.util.Date }.getTime)) }
  }

  def getUrl(file: String) = {
    Play.configuration.getString("contenturl") match {
      case Some(contentUrl) => contentUrl + controllers.routes.RemoteAssets.at(file).url
      case None => controllers.routes.RemoteAssets.at(file)
    }
  }

  def at(path: String, file: String) = getAsset(path, file)
}