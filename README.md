sbt-plugins
================

This project is largely based on the work done by Robey Pointer
which can be found at https://github.com/twitter/sbt-package-dist.  Robey, if you find this repository, thank you for
your work, inspiration and for making it easier to figure out how to bend SBT to our will.

sbt-plugins is a set of plugins for SBT 0.12.x which unites processes and tasks that TrafficLand
uses daily for release and version management.  We thought
they might be useful to others so we moved the parts that are specific to TrafficLand to a private repository
and open sourced the rest.  

We hope this project proves useful to others.

General Usage
=============

## Getting sbt-plugins

See http://www.scala-sbt.org/0.12.1/docs/Extending/Plugins for information on adding plugins.

The plugin is currently hosted by the Scala SBT repository (http://repo.scala-sbt.org/).

Add the following to your project/plugins.sbt file:

    resolvers += Resolver.url("Artifactory Online", url("http://repo.scala-sbt.org/scalasbt/repo"))(Resolver.ivyStylePatterns)
    
    addSbtPlugin("com.trafficland" % "sbt-plugins" % "0.7.0")

## Mixing in the StandardPluginSet

Using StandardPluginSet.plugs is the quickest way to add all of the plugins and settings to a new project
(the exception is the Play20Plugin which must be added explicitly). Each plugin provides a set of SBT Settings,
Tasks and/or Commands that your project can use.

#### Using an .sbt file

If you want to include all of the plugins (with the exception of the Play20Plugin):

    import trafficland.opensource.sbt.plugins._

    seq(StandardPluginSet.plugs : _*)

#### Using a .scala file (Full Build)

    package blah.blah.blah

    import sbt._
    import Keys._
    import trafficland.opensource.sbt.plugins._

    object Build extends Build {

      lazy val root = Project(id = "project-id", base = file("."),
        settings = StandardPluginSet.plugs ++
        Seq(
          //other settings
        )
      )
    }

If you don't want to include all of the plugins you add them individually:

#### Using an .sbt file

    import trafficland.sbt.plugins._

    seq(Git.plug : _*)

    seq(VersionManagement.plug : _*)

    .

    .

    .

### Using a .scala build definition

In your scala build definition, just extend the settings of any defined
projects:

    import sbt._
    import Keys._
    import trafficland.sbt.plugins._

    object MyProject extends Build {

      lazy val root = Project(id = "project-id", base = file("."),
        settings = Git.plug ++
        VersionManagement.plug ++
        other.plug ++
        other.plug ++
        Seq(
          //other settings
        )
      )
    }

Reference
=========

## Plugins

The following documentation includes intricacies specific to these plugins.  For more details about SBT see their
documentation (http://www.scala-sbt.org/0.12.1/docs/home.html) or **RTFC**.

GitPlugin
=========

The GitPlugin adds regularly used git commands to the SBT console.  The following commands are supported.

git command : SBT Console Version

- "git status" : "git-status"

- "git tag -l" : "git-show-all-tags"

- "git commit -am **commit message**" : "git-commit" **You must supply a commit message via a double quoted string.**

- "git push origin" : "git-push-origin"

- "git push origin --tags" : "git-push-origin-tags"

There are other commands but they exist to support other plugins and should not be used manually.

PackageManagementPlugin
=======================

The PackageManagementPlugin should never require interaction from the user.  However, it is important because it
 manages changes to artifact names.

Play20Plugin
============

The Play20Plugin overrides the default dist task that is included with the play-sbt-plugin and adds a source generator.

The changes to the dist task include a different folder structure when the zip file is uncompressed
and the inclusion of project specific configuration files to the zip file.

The source generator creates an AppInfo object in the target/scala-version/src_managed that can be used as to create
an endpoint for status and version checks.

ReleaseManagementPlugin
=======================

The ReleaseManagementPlugin adds two commands to the SBT console; release-final and release-snapshot.

These commands take into account whether a project is a library or an application through the setting isApp.  If
the project is a library and repository is configured the artifacts will be published.

By default isApp := true.  This disables the publishing tasks.  If set to false an attempt will be made to publish-local
and publish to configured repositories.

The release tasks are as follows:

**For applications:**

    lazy val releaseAppSnapshotTasks = releaseAppSnapshotSettingKey := Seq(
      "release-ready",
      "git-release-commit",
      "git-tag",
      "git-push-origin",
      "git-push-origin-tags"
    )

    lazy val releaseAppFinalTasks = releaseAppFinalSettingKey := Seq(
      "release-ready",
      "version-to-stable",
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


**For libraries:**

    lazy val releasePublishLibSnapshotTasks = releasePublishLibSnapshotSettingKey := Seq(
      "release-ready",
      "publish-local",
      "publish",
      "git-release-commit",
      "git-tag",
      "git-push-origin",
      "git-push-origin-tags"
    )

    lazy val releasePublishLibFinalTasks = releasePublishLibFinalSettingKey := Seq(
      "release-ready",
      "version-to-stable",
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

This makes producing a release very easy.

ScalaConfigurationPlugin
========================

Sets the current Scala version and some common compiler options.

NOTE: Play20 overrides these values so you may still have to set them in a Play20 project even if you use the
Play20Plugin.

VersionManagementPlugin
=======================

For the most part the VersionManagement plugin exists in support of the ReleaseManagementPlugin but does add some useful
tasks to the SBT console.

The following tasks are available:

- version-bump-major

- version-bump-minor

- version-bump-patch

- version-to-snapshot **Adds -SNAPSHOT to the version number.**

- version-to-stable **Removes -SNAPSHOT from the version number.**

- version-set

A note on version format.  The VersionManagementPlugin was written with semantic versioning (see http://semver.org/) of
the form major.minor.patch(-SNAPSHOT) in mind.  LEARN IT.  LIVE IT. LOVE IT.  Most of the commands should provide some error
checking to ensure a version with an invalid format is not used.

When using the PackageManagementPlugin SBT will never produce an artifact with the -SNAPSHOT classifier at the end of the
 artifact name.  It only exists in the build configuration to specify that the current build is to be considered a
 snapshot build.  The -SNAPSHOT will be replaced with a day and time identifier.  The artifact names, WHEN PUBLISHING
  SNAPSHOT VERSIONS ONLY, will appear in the following format: major.minor.path-YYYYMMDD-XXXXXX where XXXXXX is an
  integer representing the current time.

The version should be specified in a build configuration as either

    appVersion = versionString.toReleaseFormat or version := versionString.toReleaseFormat

where versionString is the version and toReleaseFormat is an implicit conversion method that ensures the version used
to create artifacts is in the correct format (i.e. it converts -SNAPSHOT to the correct day and time values).

Keep in mind that when the version is changed the entire build configuration is rewritten (with the only change being
the versionString) and SBT restarted.

There are a few other convenience methods that can be used in SBT configurations.
See trafficland.sbt.plugins.versionmanagement.SemanticVersion for more details.