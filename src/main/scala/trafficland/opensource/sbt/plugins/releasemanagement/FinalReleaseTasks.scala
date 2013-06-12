package trafficland.opensource.sbt.plugins.releasemanagement

import sbt.SettingKey

object FinalReleaseTasks {

  lazy val releasePublishLibFinalSettingKey = SettingKey[Seq[String]] (
    "release-publish-lib-final-tasks",
    "a list of tasks to execute (in order) for publishing a library's final release"
  )

  lazy val releasePublishLibFinalTasks = releasePublishLibFinalSettingKey := Seq(
    "release-ready",
    "version-to-final",
    "publish-local",
    "publish",
    "git-release-commit",
    "git-checkout-master",
    "git-merge-develop",
    "git-tag",
    "git-push-origin",
    "git-push-origin-tags",
    "git-checkout-develop",
    "version-bump-patch",
    "version-to-snapshot",
    "git-version-bump-commit",
    "git-push-origin"
  )

  lazy val releaseAppFinalSettingKey = SettingKey[Seq[String]] (
    "release-app-final-tasks",
    "a list of tasks to execute (in order) for releasing an app's final release"
  )

  lazy val releaseAppFinalTasks = releaseAppFinalSettingKey := Seq(
    "release-ready",
    "version-to-final",
    "git-release-commit",
    "git-checkout-master",
    "git-merge-develop",
    "git-tag",
    "git-push-origin",
    "git-push-origin-tags",
    "git-checkout-develop",
    "version-bump-patch",
    "version-to-snapshot",
    "git-version-bump-commit",
    "git-push-origin"
  )

}

