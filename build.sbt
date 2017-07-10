import com.typesafe.config._

name := """prosa-blog-server"""

val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()

version := "0.3.2.3"

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(SbtWeb).enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "buildinfo"
  )

scalaVersion := "2.11.8"


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
  cache,
  ws,
  evolutions,
  specs2 % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "com.typesafe.play" %% "play-mailer" % "3.0.1",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "com.h2database" % "h2" % "1.4.187",
  "com.typesafe.play" %% "play-slick" % "2.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.2",
  "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.3",
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.0.0",
  "jp.t2v" %% "play2-auth"      % "0.14.2",
  "jp.t2v" %% "play2-auth-test" % "0.14.2" % "test",
  "com.andersen-gott" %% "scravatar" % "1.0.3",
  "org.jsoup" % "jsoup" % "1.8.3",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "com.amazonaws" % "aws-java-sdk" % "1.10.0",
  "com.github.seratch" %% "awscala" % "0.5.+",
  "org.scalaz" %% "scalaz-core" % "7.2.14",
  "com.mohiva" %% "play-html-compressor" % "0.6.3")


resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

incOptions := incOptions.value.withNameHashing(true)

pipelineStages := Seq(digest, gzip)

routesGenerator := InjectedRoutesGenerator
