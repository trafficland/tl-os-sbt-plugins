package trafficland.opensource.sbt.plugins.releasemanagement

import sbt._
import Keys._
import trafficland.opensource.sbt.plugins.git.GitPlugin
import GitPlugin._
import trafficland.opensource.sbt.plugins._
import scala.language.postfixOps

object ReleaseManagementPlugin extends Plugin {

  lazy val plug = Seq(

    isApp := true,

    commands ++= Seq(releaseSnapshot, releaseFinal),

    FinalReleaseTasks.releaseAppFinalTasks,
    FinalReleaseTasks.releasePublishLibFinalTasks,
    SnapshotReleaseTasks.releasePublishLibSnapshotTasks,
    SnapshotReleaseTasks.releaseAppSnapshotTasks,

    releaseReady <<= (gitIsCleanWorkingTree, version, libraryDependencies, streams) map { (isClean, ver, deps, stream) =>
      val finalVersion = ver.toFinal.toReleaseFormat()
      stream.log.info("stable version %s".format(finalVersion))
      val tags = ("git tag -l %s".format(finalVersion) !!).trim

      // we don't release dirty trees
      if (!isClean) {
        stream.log.error("Working directory is not clean.")
        false
      }
      // we don't double-release
      else if (tags.contains(finalVersion)) {
        stream.log.error("Cannot tag release version %s: tag already exists.".format(finalVersion))
        false
      } else {
        stream.log.info("Current project is ok for release.")
        true
      }
    }

  )

  val releaseReady = TaskKey[Boolean] (
    "release-ready",
    "checks to see if current source tree and project can be published"
  )

  val isApp = SettingKey[Boolean] ("is-app", "Used by the release commands to determine if the release should be published.  " +
    "If isApp is set to true (default) then the release will not be published.")

  def releaseSnapshot = Command.command(
    "releaseSnapshot",
    "Tag and release a snapshot version of an app or lib.",
    ""
  ) (release(SnapshotRelease()))

  def releaseFinal = Command.command(
    "releaseFinal",
    "Tag and release a final version of an app or lib by removing SNAPSHOT from the version and bumping the patch value.",
    ""
  ) (release(FinalRelease()))

  def release(releaseType:ReleaseType) : (State) => State = { (state: State) =>
    state.log.info(releaseType.toString)
    val extracted = Project.extract(state)

    releaseType.isValidReleaseVersion(extracted.get(version)) match {
      case false => {
        state.log.error("Attempting snapshot release but version is set to final version.")
        state.fail
      }
      case true => Project.runTask(releaseReady, state) match {
        // returned if releaseReady doesn't exist in the current state
        case None => {
          state.log.error("no release-ready task defined")
          state.fail
        }
        // returned if releaseReady failed
        case Some((s, Inc(i))) => {
          Incomplete.show(i.tpe)
          state.fail
        }
        case Some((s, Value(false))) => {
          state.log.error("Stopping release.")
          state.fail
        }
        case Some((s, Value(true))) => {
          // we're ok for release, so return a new state with the publish tasks appended
          val pubTasks = extracted.get(releaseType.getReleaseTasks(extracted.get(isApp)))
          s.copy(remainingCommands = pubTasks ++ s.remainingCommands)
        }
      }
    }
  }
}
