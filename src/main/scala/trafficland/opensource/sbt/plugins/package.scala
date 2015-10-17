package trafficland.opensource.sbt

import scala.language.implicitConversions
import plugins.versionmanagement.SemanticVersion
import trafficland.opensource.sbt.plugins.generators.AppInfoPlugin

package object plugins {

  implicit def toVersion(originalVersion:String) : SemanticVersion = SemanticVersion.toVersion(originalVersion)
  val isApp = releasemanagement.ReleaseManagementPlugin.isApp

  val Git = git.GitPlugin
  val PackageManagement = packagemanagement.PackageManagementPlugin
  val Play20 = play20.Play20Plugin
  val ReleaseManagement = releasemanagement.ReleaseManagementPlugin
  val ScalaConfiguration = scalaconfiguration.ScalaConfigurationPlugin
  val VersionManagement = versionmanagement.VersionManagementPlugin
  val AppInfo = AppInfoPlugin

}
