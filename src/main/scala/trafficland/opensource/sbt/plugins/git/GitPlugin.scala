package trafficland.opensource.sbt.plugins.git


import sbt._
import Keys._
import trafficland.opensource.sbt.plugins._

object GitPlugin extends Plugin {

  lazy val plug = Seq(

    gitIsRepository <<= (streams) map { (stream) =>
      ("git status" !!).trim match {
        case "fatal: Not a git repository (or any of the parent directories): .git" => {
          stream.log.info("This project is not part of a git repository.")
          false
        }
        case _ => {
          stream.log.info("This project is part of a git repository.")
          true
        }
      }
    },

    gitStatus <<= (streams) map { stream =>
      val status = ("git status" !!)
      stream.log.info(status)
      status
    },

    gitIsCleanWorkingTree <<= (gitStatus, streams) map  { (status, stream) =>
      status.contains("nothing to commit, working directory clean") match {
        case true => {
          stream.log.info("Working tree is clean.")
          true
        }
        case false => {
          stream.log.info("Working tree is not clean.")
          false
        }
      }
    },

    gitBranchName <<= (gitIsRepository, streams) map { (isRepo, stream) =>
      ifRepo(isRepo) {
        val branchName = ("git symbolic-ref -q HEAD" #|| "git rev-parse HEAD" !!).trim
        stream.log.info(branchName)
        branchName
      }
    },

    gitShowTags <<= (version, streams) map { (ver, stream) =>
      val finalVersion = ver.toFinal.toReleaseFormat()
      val tags = ("git tag -l %s".format(finalVersion) !!).trim
      stream.log.info(tags)
      tags
    },

    gitShowAllTags <<= streams map { stream =>
      val tags = ("git tag -l" !!)
      stream.log.info(tags)
      tags
    },

    gitTagName <<= (name, version, streams) map { (name, ver, stream) =>
      val tagName = "%s-%s".format(name, ver)
      stream.log.info("Created tag name %s.".format(tagName))
      tagName
    },

    gitTag <<= (gitBranchName, gitTagName, streams) map { (branchName, tagName, stream) =>
      stream.log.info("Tagging branch %s with tag name %s.".format(branchName.get, tagName))
      ("git tag -m %s %s".format(tagName, tagName)).run(false).exitValue
    },

    gitVersionBumpCommitMessage <<= (version, streams) map { (ver, stream) =>
      val versionBumpMessage = "Version bumped to %s".format(ver)
      stream.log.info(versionBumpMessage)
      versionBumpMessage
    },

    gitVersionBumpCommit <<= (gitVersionBumpCommitMessage, version, streams) map { (versionBumpMessage, ver, stream) =>
      stream.log.info("Creating version bump commit for version %s.".format(ver))
      runGitCommit(versionBumpMessage)
    },

    gitReleaseCommitMessage <<= (version, streams) map { (ver, stream) =>
      val releaseMessage = "release commit for %s".format(ver)
      stream.log.info(releaseMessage)
      releaseMessage
    },

    gitReleaseCommit <<= (gitReleaseCommitMessage, version, streams) map { (releaseMessage, ver, stream) =>
      stream.log.info("Creating release commit for version %s.".format(ver))
      runGitCommit(releaseMessage)
    },

    gitCommit <<= inputTask { argTask =>
      (argTask, streams) map { (args, stream) =>
        if (args.isEmpty || args.length > 1)  throw InvalidCommitMessageException()

        val commitMessage = args(0)
        stream.log.info("Creating commit with message '%s'".format(commitMessage))
        runGitCommit(commitMessage)
      }
    },

    gitPushOrigin <<= streams map { stream =>
      stream.log.info("Pushing commits to remote repository.")
      ("git push origin").run(false).exitValue
    },

    gitPushOriginTags <<= streams map { stream =>
      stream.log.info("Pushing tags to remote repository.")
      ("git push origin --tags").run(false).exitValue
    },

    aggregate in gitReleaseCommit := false,
    aggregate in gitTag := false,

    gitCheckoutMaster <<= streams map { stream =>
      stream.log.info("Checking out master.")
      ("git checkout master").run(false).exitValue
    },

    gitCheckoutDevelop <<= streams map { stream =>
      stream.log.info("Checking out develop.")
      ("git checkout develop").run(false).exitValue
    },

    gitMergeDevelop <<= streams map { stream =>
      stream.log.info("Merging develop into master.")
      ("git merge develop").run(false).exitValue
    }
  )

  val gitIsRepository = TaskKey[Boolean](
    "git-is-repository",
    "true if this is project is inside a git repo"
  )

  val gitStatus = TaskKey[String](
    "git-status",
    "prints the current git repository status"
  )

  val gitIsCleanWorkingTree = TaskKey[Boolean](
    "git-is-clean-working-tree",
    "Checks if all tracked files have been committed"
  )

  val gitBranchName = TaskKey[Option[String]](
    "git-branch-name",
    "the name of the current git branch"
  )

  val gitShowTags = TaskKey[String](
    "git-show-tags",
    "Returns all of the tags in the repository with the final release version (the current specified version without SNAPSHOT."
  )

  val gitShowAllTags = TaskKey[String](
    "git-show-all-tags",
    "Returns all of the tags in the repository."
  )

  val gitTag = TaskKey[Int](
    "git-tag",
    "tag the project with the current version"
  )

  val gitTagName = TaskKey[String](
    "git-tag-name",
    "define a tag name for the current project version (used by git-tag)"
  )

  val gitVersionBumpCommitMessage = TaskKey[String](
    "git-version-bump-commit-message",
    "create a commit message for a version bump commit of this project (used by git-version-bump-commit)."
  )

  val gitVersionBumpCommit = TaskKey[Int](
    "git-version-bump-commit",
    "commit after bumping the version number of this project. Automatically generates the commit message."
  )


  val gitReleaseCommitMessage = TaskKey[String](
    "git-release-commit-message",
    "create a commit message for a new release of this project (used by git-release-commit)"
  )

  val gitReleaseCommit = TaskKey[Int](
    "git-release-commit",
    "commit pending changes to this project (usually as part of publishing a release). Automatically generates the commit message."
  )

  val gitCommit = InputKey[Unit](
    "git-commit",
    "commit pending changes to this project (usually as part of publishing a release). You must provide a commit message."
  )

  val gitPushOrigin = TaskKey[Unit](
    "git-push-origin",
    "pushes any commits to the remote repository."
  )

  val gitPushOriginTags = TaskKey[Unit](
    "git-push-origin-tags",
    "pushes all tags to the remote repository."
  )

  val gitCheckoutMaster = TaskKey[Unit](
    "git-checkout-master",
    "Checkout the master branch.  This is usually used for doing a release."
  )

  val gitCheckoutDevelop = TaskKey[Unit](
    "git-checkout-develop",
    "Checkout the develop branch.  This is usually used for doing a release."
  )

  val gitMergeDevelop = TaskKey[Unit](
    "git-merge-develop",
    "Merge the develop branch into the current branch."
  )

  def runGitCommit(commitMessage:String) : Int = {
    val pb = ("git add ." #&& Seq("git", "commit", "-m", "'%s'".format(commitMessage)))
    val proc = pb.run(false)
    proc.exitValue
  }

  def ifRepo[T](isRepo: Boolean)(f: => T): Option[T] = {
    if (isRepo) {
      Some(f)
    } else {
      None
    }
  }
}
