package services

import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.JavaConverters.asJavaIterableConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.collection.JavaConverters.mapAsScalaMapConverter
import scala.language.postfixOps

import anorm.SQL
import anorm.SqlParser.flatten
import anorm.SqlParser.get
import anorm.SqlParser.str
import anorm.sqlToSimple
import anorm.toParameterValue
import play.api.Play.current
import play.api.db.DB

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

  def tableToIterable(map: java.util.Map[Number, Number]): java.lang.Iterable[java.util.Map[String, java.math.BigDecimal]] =
    map.asScala.map { (en) =>
      Map(
        "x" -> (new java.math.BigDecimal(en._1.doubleValue())),
        "y" -> (new java.math.BigDecimal(en._2.doubleValue()))).asJava
    }.asJava

  def findcreativestats(campaignid: Long, from: Date, to: Date, prec: Integer): Option[List[(Long, Long, String, java.math.BigDecimal)]] = {
    val from_date = df.format(from).substring(0, prec)
    val to_date = df.format(to).substring(0, prec)

    DB.withConnection { implicit c =>
      val rows: List[(Long, Long, String, java.math.BigDecimal)] = SQL(
        """
      select c.campaign_id as cid,c.id as id,substr(cs.timestep,1,{p}) as ts,sum(cs.views) as views 
      from creative_stat_data cs 
      join creative c on c.id = cs.creative_id 
      where substr(cs.timestep,1,{p}) between {tsf} and {tst} and c.campaign_id = {cid} 
      group by c.id,cs.timestep;
    """)
        .on("p" -> prec, "tsf" -> from_date, "tst" -> to_date, "cid" -> campaignid)
        .as(get[Long]("cid") ~ get[Long]("id") ~ str("ts") ~ get[java.math.BigDecimal]("views") map (flatten) *);
      return Some(rows)
    }
  }

  def findcookiestats(audienceid: Long, from: Date, to: Date, prec: Integer): Option[List[(Long, Long, String, String, java.math.BigDecimal)]] = {
    val from_date = df.format(from).substring(0, prec)
    val to_date = df.format(to).substring(0, prec)

    DB.withConnection { implicit c =>
      val rows: List[(Long, Long, String, String, java.math.BigDecimal)] = SQL(
        """
      select c.audience_id as cid,c.id as id,substr(cs.timestep,1,{p}) as ts,coalesce(cs.sub,'') as sub,sum(cs.views) as views 
      from cookie_stat_data cs 
      join cookie c on c.id = cs.cookie_id 
      where substr(cs.timestep,1,{p}) between {tsf} and {tst} and c.audience_id = {aid} 
      group by c.id,cs.timestep,cs.sub;
    """)
        .on("p" -> prec, "tsf" -> from_date, "tst" -> to_date, "aid" -> audienceid)
        .as(get[Long]("cid") ~ get[Long]("id") ~ str("ts") ~ str("sub") ~ get[java.math.BigDecimal]("views") map (flatten) *);
      return Some(rows)
    }
  }
}