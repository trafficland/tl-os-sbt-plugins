package trafficland.opensource.sbt.plugins.scalaconfiguration

import sbt._
import sbt.Keys._

object ScalaConfigurationPlugin extends Plugin {

  lazy val plug = Seq(
    scalaVersion  := "2.10.0",
    scalacOptions := Seq("-deprecation", "-encoding", "utf8", "-feature", "-language:postfixOps", "-language:implicitConversions")
  )
}
