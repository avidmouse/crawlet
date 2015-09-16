name := "crawlet"

organization := "com.avidmouse"

version := "2.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-language:reflectiveCalls", "-language:postfixOps", "-language:higherKinds", "-language:existentials")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.0-RC2",
  "com.typesafe.akka" %% "akka-http-experimental" % "1.0" exclude("com.typesafe.akka", "akka-actor"),
  "org.jsoup" % "jsoup" % "1.8.3" % "test"
)
