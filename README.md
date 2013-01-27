
sbt-plugins
================

This project is largely based on the work done by Robey Pointer
which can be found at https://github.com/twitter/sbt-package-dist.

sbt-plugins is a set of plugins for SBT 0.12.1 which unites processes and tasks that TrafficLand
uses daily for release and version management.  We thought
they might be useful to others so we moved the parts that are specific to TrafficLand to a private repository
and open sourced the rest.  

We hope this project proves useful to others.

Included in the current release (0.6.2):

- **Release Management** - tasks that automate the release process including changes to version numbers, publishing and
    git repository commits and pushes.
- **Play Framework 2.0** - common tasks we use in the distribution of Play applications including a custom dist task that
    packages our applications with the correct files using our naming conventions.
- **Git** - adds common git commands.  This plugin supports a lot of the release
    management tasks but can be used at the SBT console with the SBT format.
- **Package Management** - configures the naming of our artifacts in a administratively friendly way.
- **Publishing** - configures SBT for our Artifactory server and the location of our Artifactory credentials.
- **Scala Configuration** - sets TrafficLand's defaults for the Scala compiler.
- **Version Management** - adds tasks for changing the project version which can be used manually but exist in support of
    the release management plugin.
- **TrafficLand Default Project Settings** - adds some default settings to a project
- **TrafficLand Standard Plugin Set** - a convenient way to add the most commonly used TrafficLand plugins to a project.

General Usage
=============

## Getting tl-sbt-plugins

See http://www.scala-sbt.org/0.12.1/docs/Extending/Plugins for information on adding plugins.
In general, you'll need to add the following to your project/plugins.sbt file:

    resolvers += Resolver.url("TrafficLand Artifactory Server", url("http://build01.tl.com:8081/artifactory/repo"))(Resolver.ivyStylePatterns)

    addSbtPlugin("com.trafficland" % "tl-sbt-plugins" % "0.6.2")

## Mixing in TrafficLandStandardPluginSet

Using TrafficLandStandardPluginSet.plugs is the quickest way to add all of the plugins and settings to a new project
(the exception is the Play20Plugin which must be added explicitly). Each plugin provides a set of SBT Settings,
Tasks and/or Commands that your project can use.

#### Using an .sbt file

If you want to include all of the plugins (with the exception of the Play20Plugin):

    import trafficland.sbt.plugins.TrafficLandStandardPluginSet

    seq(TrafficLandStandardPluginSet.plugs : _*)

#### Using a .scala file (Full Build)

    package blah.blah.blah

    import sbt._
    import Keys._
    import trafficland.sbt.plugins.TrafficLandStandardPluginSet

    object TrafficLandSbtPluginsBuild extends Build {

      lazy val root = Project(id = "tl-sbt-plugins", base = file("."),
        settings = TrafficLandStandardPluginSet.plugs ++
        Seq(
          //other settings
        )
      )
    }

If you don't want to include all of the plugins you add them individually:

#### Using an .sbt file

    import trafficland.sbt.plugins.<plugin package>.<plugin name> (plans exist to simplify importing individual plugins)

    seq(GitPlugin.plug : _*)

    seq(VersionManagementPlugin.plug : _*)

    .

    .

    .

### Using a .scala build definition

In your scala build definition, just extend the settings of any defined
projects:

    import sbt._
    import Keys._
    import trafficland.sbt.plugins.<plugin package>.<plugin name> (plans exist to simplify importing individual plugins)

    object MyProject extends Build {

      lazy val root = Project(id = "tl-sbt-plugins", base = file("."),
        settings = GitPlugin.plug ++
        VersionManagementPlugin.plug ++
        . ++
        . ++
        . ++
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
 manages changes TrafficLand makes to our artifacts' names.  Without the changes this plugin makes to the artifact names
 we cannot interoperate with our Artifactory server.

Play20Plugin
============

The Play20Plugin overrides the default dist task that is included with the play-sbt-plugin and adds a source generator.

The changes to the dist task include a different folder structure when the zip file is uncompressed
and the inclusion of TrafficLand specific configuration files to the zip file.

The source generator creates an AppInfo object in the target/scala-version/src_managed that can be used as to create
an endpoint for status and version checks.

ReleaseManagementPlugin
=======================

The ReleaseManagementPlugin adds two commands to the SBT console; release-final and release-snapshot.

These commands take into account whether a project is a library or an application, the former requiring publication to our
Artifactory server, through the setting isApp.

By default isApp := true.  This disables the publishing tasks.  If set to fault an attempt will be made to publish-local
and publish to TrafficLand's Artifactory server.

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
      "git-tag",
      "version-bump-patch",
      "version-to-snapshot",
      "git-version-bump-commit",
      "git-push-origin",
      "git-push-origin-tags"
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
      "git-tag",
      "version-bump-patch",
      "version-to-snapshot",
      "git-version-bump-commit",
      "git-push-origin",
      "git-push-origin-tags"
    )

This makes producing a release very easy.

NOTE: The release-final command should only be run from the develop branch of a given project.
Further, there is a step that has not been written as of the latest version.
Once a final release has been completed the developer has to manually checkout the master branch and run the following
command: git merge **release tag name**.  This will merge the released version AS IT WAS BUILT FOR RELEASE into the master
 branch.  Nothing should every be changed on the master branch.

ScalaConfigurationPlugin
========================

Sets the current Scala version in use at TrafficLand with some compiler options.

NOTE: Play20 overrides these values so you may still have to set them in a Play20 project even if you use the
TrafficLandPlay20PluginSet.

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

A note on version format.  TrafficLand uses semantic versioning (see http://semver.org/)
of the form major.minor.patch(-SNAPSHOT).  LEARN IT.  LIVE IT. LOVE IT.  Most of the commands should provide some error
checking to ensure a version with an invalid format is not used.

When using the PackageManagementPlugin SBT will never produce an artifact with the -SNAPSHOT classifier at the end of the
 artifact name.  It only exists in the build configuration to specify that the current build is to be considered a
 snapshot build.  The -SNAPSHOT will be replaced with a day and time identifier.  The artifact names, WHEN PUBLISHING
  SNAPSHOT VERSIONS ONLY, will appear in the following format: major.minor.path-YYYYMMDD-XXXXXX where XXXXXX is an
  integer representing the current time.

The version should be specified in a build configuration as either

    appVersion = versionString.toReleaseFormat or version := versionString.toReleaseFormat

where versionString is the version and toReleaseFormat is an implicit conversion method that ensures the version used
to create artifacts is in the correct format (i.e. it converts -SNAPSHOT to the correct day and time values).  As of the
current release trafficland.sbt.plugins.versionmanagement.SemanticVersion._ must be imported to access toReleaseFormat.
  Plans exist to simplify this.

Keep in mind that when the version is changed the entire build configuration is rewritten (with the only change being
the versionString) and SBT restarted.

There are a few other convenience methods that can be used in SBT configurations.
See trafficland.sbt.plugins.versionmanagement.SemanticVersion for more details.