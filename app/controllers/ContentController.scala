package controllers

import models._
import services._

import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import services.SendMail

import views.html

import play.Logger

object ContentController extends Controller {
  def cookie = (uuid: String, sub: String) => Action { implicit request =>
    Ok("")
  }

  def creative = (uuid: String) => Action { implicit request =>
    Ok("")
  }
}

