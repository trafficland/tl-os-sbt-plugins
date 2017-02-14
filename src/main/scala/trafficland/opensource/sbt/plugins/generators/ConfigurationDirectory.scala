package trafficland.opensource.sbt.plugins.generators

import sbt._
import java.io.File
import sbt.Keys._

trait ConfigurationDirectory {
  /**
   * Configuration directory often "conf".
   */
  val confDirectory = SettingKey[File]("conf-directory")
}

object DefaultConfigurationDirectory extends Plugin with ConfigurationDirectory {
  override lazy val projectSettings = Seq(
    confDirectory <<= baseDirectory apply { bd => bd / "conf"}
  )
}