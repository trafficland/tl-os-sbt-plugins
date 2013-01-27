package trafficland.sbt.plugins.releasemanagement

import sbt.SettingKey

object SnapshotReleaseTasks {

  lazy val releasePublishLibSnapshotSettingKey = SettingKey[Seq[String]] (
    "release-publish-lib-snapshot-tasks",
    "a list of tasks to execute (in order) for publishing a library's snapshot release"
  )

  lazy val releasePublishLibSnapshotTasks = releasePublishLibSnapshotSettingKey := Seq(
    "release-ready",
    "publish-local",
    "publish",
    "git-release-commit",
    "git-tag",
    "git-push-origin",
    "git-push-origin-tags"
  )

  lazy val releaseAppSnapshotSettingKey = SettingKey[Seq[String]] (
    "release-app-snapshot-tasks",
    "a list of tasks to execute (in order) for releasing an app's snapshot release"
  )

  lazy val releaseAppSnapshotTasks = releaseAppSnapshotSettingKey := Seq(
    "release-ready",
    "git-release-commit",
    "git-tag",
    "git-push-origin",
    "git-push-origin-tags"
  )

}
