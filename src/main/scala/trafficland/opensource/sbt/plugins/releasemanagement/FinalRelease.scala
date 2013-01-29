package trafficland.opensource.sbt.plugins.releasemanagement

import sbt.SettingKey

case class FinalRelease() extends ReleaseType() {

  protected val appReleaseTasks: SettingKey[Seq[String]] = FinalReleaseTasks.releaseAppFinalSettingKey

  protected val libReleaseTasks: SettingKey[Seq[String]] = FinalReleaseTasks.releasePublishLibFinalSettingKey

  def isValidReleaseVersion(version: String): Boolean = true
}