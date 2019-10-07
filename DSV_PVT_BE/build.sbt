name := """play-java"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "com.restfb" % "restfb" % "1.6.16",
  "com.feth" %% "play-authenticate" % "0.7.1",
  "mysql" % "mysql-connector-java" % "5.1.34",
  "org.json"%"org.json"%"chargebee-1.0",
  javaJdbc,
  cache,
  javaWs
)
