name := "aranha"

version := "1.0"

scalaVersion := "2.11.2"

scalacOptions ++= Seq(
  "-language:dynamics",
  "-deprecation",
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-language:experimental.macros")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.42.2",
  "org.seleniumhq.selenium" % "selenium-support" % "2.42.2",
  "org.jodd" % "jodd-lagarto" % "3.5.2", //  % "provided"
  "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.8",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "org.slf4j" % "slf4j-log4j12" % "1.7.7",
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.1.2" % "test",
  "net.jcip" % "jcip-annotations" % "1.0" % "test",
  "org.seleniumhq.selenium" % "selenium-remote-driver" % "2.42.2" % "test",
  "com.github.detro.ghostdriver" % "phantomjsdriver" % "1.1.0" % "test",
  "org.seleniumhq.selenium" % "selenium-firefox-driver" % "2.42.2" % "test"
)


