ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.18"
ThisBuild / organization := "com.fraud"

lazy val root = (project in file("."))
  .settings(
    name := "fraud-detection",

    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.4.1",
      "org.apache.spark" %% "spark-sql" % "3.4.1",
      "org.apache.spark" %% "spark-streaming" % "3.4.1",
      "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.4.1",

      "org.apache.kafka" % "kafka-clients" % "3.5.1",

      "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.2",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.2",

      "software.amazon.awssdk" % "s3" % "2.20.55",
      "software.amazon.awssdk" % "sns" % "2.20.55",

      "ch.qos.logback" % "logback-classic" % "1.4.11",
      "org.slf4j" % "slf4j-api" % "2.0.9",

      "org.scalatest" %% "scalatest" % "3.2.16" % Test

    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % "0.14.5",
      "io.circe" %% "circe-generic" % "0.14.5",
      "io.circe" %% "circe-parser" % "0.14.5"
    ),
    libraryDependencies += "com.opencsv" % "opencsv" % "5.7.1"
,libraryDependencies ++= Seq(
      "org.apache.kafka" % "kafka-clients" % "3.6.0",
      "io.circe" %% "circe-core" % "0.14.5",
      "io.circe" %% "circe-generic" % "0.14.5",
      "io.circe" %% "circe-parser" % "0.14.5"
    ),
      fork := true,
    javaOptions ++= Seq(
      "--add-exports", "java.base/sun.nio.ch=ALL-UNNAMED",
      "--add-opens", "java.base/java.nio=ALL-UNNAMED"
    )
  )


