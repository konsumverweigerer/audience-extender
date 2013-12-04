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
  def cookie = (uuid: String, sub: String) => Action { implicit request =>
    Ok("")
  }

  def creative = (uuid: String) => Action { implicit request =>
    Ok("")
  }
}

