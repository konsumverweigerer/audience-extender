package services

import anorm._
import java.text.SimpleDateFormat
import java.util.Date
import play.api.db.DB

object StatsHandler {
  import play.api.Play.current
  val df = new SimpleDateFormat("yyyyMMddHH")

  def countcookie(cookieid: Long, sub: String) = {
    val timestep = df.format(new Date())
    DB.withConnection { implicit c =>
      val result: Boolean = SQL("Select 1").execute()
    }
  }

  def countcreative(cookieid: Long) = {
    val timestep = df.format(new Date())
  }
}