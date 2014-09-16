name := "anorm-postgresql"

organization := "com.derekschaefer"

version := "0.3"

scalaVersion := "2.10.4"

crossScalaVersions := Seq("2.10.4","2.11.2")

scalacOptions ++= Seq(
  "-feature", "-unchecked", "-deprecation",
  "-language:postfixOps"
)

resolvers ++= Seq(
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "postgresql" % "postgresql" % "9.2-1002.jdbc4",
  "com.typesafe.play" %% "anorm" % "2.3.4",
  "joda-time" % "joda-time" % "2.4",
  "org.joda" % "joda-convert" % "1.7"
)
