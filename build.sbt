name := "StockMarket"

version := "1.0"

scalaVersion := "2.13.8"

val slick = "3.3.2"
val scalaLoggingVersion = "3.9.4"
val slf4jVersion = "1.7.36"
val logbackVersion = "1.2.11"
val akkaHttp = "10.2.4"
val akkaActor = "2.6.8"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % slick,
  "com.typesafe.slick" %% "slick-hikaricp" % slick,
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "com.microsoft.sqlserver" % "mssql-jdbc" % "9.4.0.jre11",
  "org.typelevel" %% "cats-core" % "2.2.0",

  "com.typesafe.akka" %% "akka-stream" % akkaActor,
  "com.typesafe.akka" %% "akka-http" % akkaHttp,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttp,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaActor,

  "org.scalatestplus" %% "scalacheck-1-15" % "3.2.7.0" % Test,
  "com.codecommit" %% "cats-effect-testing-scalatest" % "0.4.1" % Test,
)