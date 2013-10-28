name := "audience-extender"

version := "0.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  "org.webjars" %% "webjars-play" % "2.2.0",
  "edu.vt.middleware" % "vt-password" % "3.1.2",
  cache
)     

play.Project.playJavaSettings
