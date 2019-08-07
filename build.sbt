name := "udemy-akka-essentials"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion = "2.5.13"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5"
