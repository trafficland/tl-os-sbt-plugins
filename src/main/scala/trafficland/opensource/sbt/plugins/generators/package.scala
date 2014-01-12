package trafficland.opensource.sbt.plugins

package object generators {
  def plugs = AppInfoPlugin.plug ++
    BuildInfoPlugin.plug ++
    LogbackConfigurationPlugin.plug
}
