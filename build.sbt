name := "crawlet"

organization := "com.avidmouse"

version := "2.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.0-RC2",
  "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
  "org.jsoup" % "jsoup" % "1.8.3"
)
