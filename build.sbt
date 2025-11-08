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
    fork := true,
    javaOptions ++= Seq(
      "--add-exports", "java.base/sun.nio.ch=ALL-UNNAMED",
      "--add-opens", "java.base/java.nio=ALL-UNNAMED"
    )
  )


