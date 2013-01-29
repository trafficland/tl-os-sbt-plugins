package trafficland.opensource.sbt

import plugins.versionmanagement.SemanticVersion

package object plugins {

  implicit def toVersion(originalVersion:String) : SemanticVersion = SemanticVersion.toVersion(originalVersion)

  val Git = git.GitPlugin
  val PackageManagement = packagemanagement.PackageManagementPlugin
  val Play20 = play20.Play20Plugin
  val ReleaseManagement = releasemanagement.ReleaseManagementPlugin
  val ScalaConfiguration = scalaconfiguration.ScalaConfigurationPlugin
  val VersionManagement = versionmanagement.VersionManagementPlugin

}