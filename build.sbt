import com.typesafe.config._

name := """prosa-blog-server"""

val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()

version := conf.getString("app.version")

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.typesafe.play" % "play-slick_2.11" % "0.8.1",
  "com.typesafe.play" %% "play-mailer" % "2.4.0",
  "jp.t2v" %% "play2-auth"      % "0.13.2",
  "jp.t2v" %% "play2-auth-test" % "0.13.2" % "test",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "mysql" % "mysql-connector-java" % "5.1.31",
  "com.andersen-gott" %% "scravatar" % "1.0.3",
  "joda-time" % "joda-time" % "2.3",
  "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.3",
  "org.jsoup" % "jsoup" % "1.7.3",
  "com.amazonaws" % "aws-java-sdk" % "1.9.3",
  "com.github.seratch" %% "awscala" % "0.5.+",
  "com.mohiva" %% "play-html-compressor" % "0.3.1",
  jdbc,
  cache
)
