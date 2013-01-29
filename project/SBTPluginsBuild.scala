import sbt._
import Keys._
import trafficland.sbt.plugins.versionmanagement.SemanticVersion._
import trafficland.sbt.plugins.StandardPluginSet
import trafficland.sbt.plugins.releasemanagement.ReleaseManagementPlugin._

object SBTPluginsBuild extends Build {

  lazy val root = Project(id = "sbt-plugins", base = file("."),
    settings = StandardPluginSet.plugs ++
    Seq(
      isApp := false,
      name := "sbt-plugins",
      organization := "com.trafficland",
      organizationName := "TrafficLand, Inc.",
      sbtPlugin := true,
      version       := "0.6.3-SNAPSHOT".toReleaseFormat,
      scalaVersion := "2.9.2",
      scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
      resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "1.8" % "test"
      )
    )
  )
}
