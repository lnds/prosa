import com.typesafe.config._

name := """prosa-blog-server"""

val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()

version := conf.getString("app.version")

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  // Emit warning for usages of deprecated APIs
  "-deprecation"
  // Emit warning for usages of features that should be imported explicitly
  , "-feature"
  // Enable additional warnings where generated code depends on assumptions
  , "-unchecked"
  // Fail the compilation if there are any warnings
  , "-Xfatal-warnings"
  // Enable or disable specific warnings
  , "-Xlint:_"
)

scalacOptions ++= Seq(
  // Do not adapt an argument list to match the receiver
  "-Yno-adapted-args"
  // Warn when dead code is identified
  , "-Ywarn-dead-code"
  // Warn when local and private vals, vars, defs, and types are are unused
  , "-Ywarn-unused"
  // Warn when imports are unused
  //, "-Ywarn-unused-import"
  // Warn when non-Unit expression results are unused
  //, "-Ywarn-value-discard"
)

scalacOptions ++= Seq(
  // Specify character encoding used by source files
  "-encoding", "UTF-8"
  // Target platform for object files
  , "-target:jvm-1.8"
  // Turn on future language features
  , "-Xfuture"
  // Compile without importing scala.*, java.lang.*, or Predef
  //, "-Yno-imports"
  // Compile without importing Predef
  //, "-Yno-predef"
)

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
  "jp.t2v" %% "play2-auth"      % "0.14.1",
  "jp.t2v" %% "play2-auth-test" % "0.14.1" % "test",
  "com.andersen-gott" %% "scravatar" % "1.0.3",
  "org.jsoup" % "jsoup" % "1.8.3",
  "com.amazonaws" % "aws-java-sdk" % "1.10.0",
  "com.github.seratch" %% "awscala" % "0.5.+",
  "com.mohiva" %% "play-html-compressor" % "0.5.0")


resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

incOptions := incOptions.value.withNameHashing(true)

pipelineStages := Seq(rjs, digest, gzip)

routesGenerator := InjectedRoutesGenerator
