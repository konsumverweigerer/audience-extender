name := "audience-extender"

version := "0.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  "org.webjars" %% "webjars-play" % "2.2.0",
  cache
)     

play.Project.playJavaSettings
