package trafficland.sbt.plugins

import sbt._
import git.GitPlugin
import packagemanagement.PackageManagementPlugin
import releasemanagement.ReleaseManagementPlugin
import scalaconfiguration.ScalaConfigurationPlugin
import versionmanagement.VersionManagementPlugin

object StandardPluginSet extends Plugin {

  lazy val plugs = Project.defaultSettings ++
    GitPlugin.plug ++
    PackageManagementPlugin.plug ++
    ReleaseManagementPlugin.plug ++
    ScalaConfigurationPlugin.plug ++
    VersionManagementPlugin.plug

}
