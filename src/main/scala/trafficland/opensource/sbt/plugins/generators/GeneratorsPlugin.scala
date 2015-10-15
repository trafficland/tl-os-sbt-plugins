package trafficland.opensource.sbt.plugins.generators

import sbt._

object GeneratorsPlugin extends Plugin {
  def plugs =  AppInfoPlugin.plug ++
    BuildInfoPlugin.plug ++
    LogbackConfigurationPlugin.plug
}
