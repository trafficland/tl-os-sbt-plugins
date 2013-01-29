package trafficland.opensource.sbt.plugins

import sbt._
import releasemanagement.ReleaseManagementPlugin
import scalaconfiguration.ScalaConfigurationPlugin
import versionmanagement.VersionManagementPlugin
import trafficland.opensource.sbt.plugins.git.GitPlugin
import trafficland.opensource.sbt.plugins.packagemanagement.PackageManagementPlugin

object StandardPluginSet extends Plugin {

  lazy val plugs = Project.defaultSettings ++
    GitPlugin.plug ++
    PackageManagementPlugin.plug ++
    ReleaseManagementPlugin.plug ++
    ScalaConfigurationPlugin.plug ++
    VersionManagementPlugin.plug

}
