name := """chat-app"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.7"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

// twitter4j
libraryDependencies += ehcache
libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.3.1"
libraryDependencies += "org.twitter4j" % "twitter4j-core" % "4.0.6"

// database
libraryDependencies ++= Seq(evolutions, jdbc)
libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"                  % "3.3.1",
  "org.scalikejdbc" %% "scalikejdbc-config"           % "3.3.1",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.6.0-scalikejdbc-3.3"
)
// libraryDependencies += "org.postgresql" % "postgresql" % "42.2.5"
libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
