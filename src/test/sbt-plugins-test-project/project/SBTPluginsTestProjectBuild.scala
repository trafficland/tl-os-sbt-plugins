import sbt._
import sbt.Keys._
import trafficland.opensource.sbt.plugins._

object SBTPluginsTestProjectBuild extends Build {

  val appVersion = "4.1.2-SNAPSHOT".toReleaseFormat
  val appName = "sbt-plugins-test-project"

  lazy val root = Project(id = appName, base = file("."), settings =
    Project.defaultSettings ++
      Git.plug ++
      PackageManagement.plug ++
      ReleaseManagement.plug ++
      ScalaConfiguration.plug ++
      VersionManagement.plug ++
    Seq(
      name := appName,
      organization := "com.trafficland",
      version       := appVersion,
      sourceGenerators in Compile <+= (sourceManaged in Compile, name, version, organizationName) map { (outDir, appName, appVersion, orgName) =>
        writeVersion(outDir, appName, appVersion, orgName)
      }
    )
  )

  def writeVersion(outDir: File, appName:String, appVersion:String, organizationName:String) = {
    val className = "AppInfo"
    val file = outDir / "%s.scala".format(className)
    IO.write(file,
      """package com.trafficland.sbtpluginstestproject

    object %s {
      val version = "%s"
      val name = "%s"
      val vendor = "%s"
    }""".format(className, appVersion, appName, organizationName))
    Seq(file)
  }
}
