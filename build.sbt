import com.typesafe.config._

name := """prosa-blog-server"""

val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()

version := conf.getString("app.version")

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  evolutions,
  specs2 % Test,
  "com.typesafe.play" %% "play-mailer" % "3.0.1",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "com.typesafe.play" %% "play-slick" % "1.1.0",
 // "com.typesafe.play" %% "play-slick-evolutions" % "1.1.0",
  "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.3",
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.0.0",
  "jp.t2v" %% "play2-auth"      % "0.14.0",
  "jp.t2v" %% "play2-auth-test" % "0.14.0" % "test",
  "com.andersen-gott" %% "scravatar" % "1.0.3",
  "org.jsoup" % "jsoup" % "1.8.3",
  "com.amazonaws" % "aws-java-sdk" % "1.10.0",
  "com.github.seratch" %% "awscala" % "0.5.+",
  "com.mohiva" %% "play-html-compressor" % "0.5.0")


resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesGenerator := InjectedRoutesGenerator

incOptions := incOptions.value.withNameHashing(true)

