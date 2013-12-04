package services

import anorm._
import anorm.SqlParser._
import anorm.RowParser._
import anorm.ResultSetParser._
import java.text.SimpleDateFormat
import java.util.Date
import play.api.db._

object StatsHandler {
  import play.api.Play.current
  import scala.language.postfixOps
  
  val df = new SimpleDateFormat("yyyyMMddHHmmss")

  def countcookie(cookieid: Long, sub: String, prec: Integer = 10) = {
    val timestep = df.format(new Date()).substring(0, prec)
    DB.withConnection { implicit c =>
      if (sub != null) {
        val result: Boolean = SQL("select update_cookie_stats_sub()").on("timestep" -> timestep,
          "id" -> cookieid, "sub" -> sub).execute()
      } else {
        val result: Boolean = SQL("select update_cookie_stats").on("timestep" -> timestep,
          "id" -> cookieid).execute()
      }
    }
  }

  def countcreative(cookieid: Long, prec: Integer = 10) = {
    val timestep = df.format(new Date()).substring(0, prec)
    DB.withConnection { implicit c =>
      val result: Boolean = SQL("select update_creative_stats()").on("timestep" -> timestep,
        "id" -> cookieid).execute()
    }
  }

  def findcreativestats(campaignid: Long, from: Date, to: Date, prec: Integer): Option[List[(Long, Long, String, Int)]] = {
    val from_date = df.format(from).substring(0, prec)
    val to_date = df.format(to).substring(0, prec)

    DB.withConnection { implicit c =>
      val rows: List[(Long, Long, String, Int)] = SQL(
        """
      select c.campaign_id as cid,c.id as id,substr(cs.timestep,1,{p}) as ts,sum(cs.views) as views 
      from creative_stat_data cs 
      join creative c on c.id = cs.creative_id 
      where substr(cs.timestep,1,{p} between {tsf} and {tst} and c.campaign_id = {cid}
      group by c.id,substr(cs.timestep,1,{p});
    """)
        .on("p" -> prec, "tsf" -> from_date, "tst" -> to_date, "cid" -> campaignid)
        .as(get[Long]("cid") ~ get[Long]("id") ~ str("ts") ~ int("views") map (flatten) *);
      return Some(rows)
    }
  }

  def findcookiestats(audienceid: Long, from: Date, to: Date, prec: Integer): Option[List[(Long, Long, String, String, Int)]] = {
    val from_date = df.format(from).substring(0, prec)
    val to_date = df.format(to).substring(0, prec)

    DB.withConnection { implicit c =>
      val rows: List[(Long, Long, String, String, Int)] = SQL(
        """
      select c.audience_id as cid,c.id as id,substr(cs.timestep,1,{p}) as ts,cs.sub as sub,sum(cs.views) as views 
      from cookie_stat_data cs 
      join cookie c on c.id = cs.cookie_id 
      where substr(cs.timestep,1,{p} between {tsf} and {tst} and c.campaign_id = {cid}
      group by c.id,substr(cs.timestep,1,{p}),sub;
    """)
        .on("p" -> prec, "tsf" -> from_date, "tst" -> to_date, "aid" -> audienceid)
        .as(get[Long]("cid") ~ get[Long]("id") ~ str("ts") ~ str("sub") ~ int("views") map (flatten) *);
      return Some(rows)
    }
  }
}