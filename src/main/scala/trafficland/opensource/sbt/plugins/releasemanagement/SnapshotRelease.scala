package trafficland.opensource.sbt.plugins.releasemanagement

import sbt.SettingKey
import trafficland.opensource.sbt.plugins._

case class SnapshotRelease() extends ReleaseType() {

  protected val appReleaseTasks: SettingKey[Seq[String]] = SnapshotReleaseTasks.releaseAppSnapshotSettingKey

  protected val libReleaseTasks: SettingKey[Seq[String]] = SnapshotReleaseTasks.releasePublishLibSnapshotSettingKey

  def isValidReleaseVersion(version: String): Boolean = version.isSnapshot
}
