import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "audience-extender"
  val appVersion = "0.5.1"

  val appDependencies = Seq(
    javaCore,
    javaJpa,
    javaJdbc,
    javaEbean,

    "org.springframework" % "spring-context" % "3.2.2.RELEASE",
    "javax.inject" % "javax.inject" % "1",

    "edu.vt.middleware" % "vt-password" % "3.1.2",
    "com.typesafe" %% "play-plugins-mailer" % "2.2.0",

    "org.springframework.data" % "spring-data-jpa" % "1.3.2.RELEASE",
    "org.hibernate" % "hibernate-entitymanager" % "3.6.10.Final",
    "postgresql" % "postgresql" % "9.1-901.jdbc4",

    "org.webjars" %% "webjars-play" % "2.2.1",
    "org.webjars" % "requirejs" % "2.1.8",
    "org.webjars" % "bootstrap" % "3.0.0" exclude("org.webjars", "jquery"),
    "org.webjars" % "knockout" % "2.3.0",
    "org.webjars" % "nvd3" % "8415ee55d3",
    "org.webjars" % "datatables-tools" % "2.1.5",
    "org.webjars" % "jquery-file-upload" % "8.4.2" exclude("org.webjars", "jquery"),
    "org.webjars" % "bootstrap-datepicker" % "1.2.0",
    "org.webjars" % "x-editable-bootstrap" % "1.5.1",
    "org.webjars" % "jqbootstrapvalidation" % "1.3.6" exclude("org.webjars", "jquery"),
    "org.mockito" % "mockito-core" % "1.9.5" % "test")

  val main = play.Project(appName, appVersion, appDependencies).settings( // ebeanEnabled := false
      requireJs += "pages/app.js",
      requireJs += "pages/dashboard.campaign.js",
      requireJs += "pages/dashboard.audience.js",
//      requireJs += "admins.js",
//      requireJs += "publishers.js",
      
      requireJsShim := "require.shim.js",
//      requireJsShim += "dashboard.audiencejs",

      sources in doc in Compile := List()
  )
}
