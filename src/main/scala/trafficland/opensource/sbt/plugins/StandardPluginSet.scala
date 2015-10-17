package trafficland.opensource.sbt.plugins

import sbt._
import releasemanagement.ReleaseManagementPlugin
import scalaconfiguration.ScalaConfigurationPlugin
import versionmanagement.VersionManagementPlugin
import trafficland.opensource.sbt.plugins.git.GitPlugin
import trafficland.opensource.sbt.plugins.packagemanagement.PackageManagementPlugin
import trafficland.opensource.sbt.plugins.rpm.CentOSRPMPlugin
import trafficland.opensource.sbt.plugins.distribute.DistributePlugin
import trafficland.opensource.sbt.plugins.generators.GeneratorsPlugin

object StandardPluginSet extends Plugin {

  lazy val plugs = Defaults.coreDefaultSettings ++
    GitPlugin.plug ++
    PackageManagementPlugin.plug ++
    ReleaseManagementPlugin.plug ++
    ScalaConfigurationPlugin.plug ++
    VersionManagementPlugin.plug ++
    GeneratorsPlugin.plugs ++
    CentOSRPMPlugin.plug ++
    DistributePlugin.plug
}
