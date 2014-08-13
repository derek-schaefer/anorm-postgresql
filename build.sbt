name := "anorm-postgresql"

organization := "com.derekschaefer"

version := "0.1"

scalaVersion := "2.10.4"

scalacOptions ++= Seq(
  "-feature", "-unchecked", "-deprecation",
  "-language:postfixOps"
)

resolvers ++= Seq(
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "postgresql" % "postgresql" % "9.2-1002.jdbc4",
  "play" %% "anorm" % "2.1.1",
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.2"
)
