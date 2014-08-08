name := """prosa-blog-server"""

version := "0.1.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "0.8.0-RC2",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "jp.t2v" %% "play2-auth"      % "0.12.0",
  "jp.t2v" %% "play2-auth-test" % "0.12.0" % "test",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "mysql" % "mysql-connector-java" % "5.1.31",
  "joda-time" % "joda-time" % "2.3",
  "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.3",
  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.0",
  "org.jsoup" % "jsoup" % "1.7.3",
  "com.amazonaws" % "aws-java-sdk" % "1.7.12",
  "com.github.seratch" %% "awscala" % "0.2.+"
)
