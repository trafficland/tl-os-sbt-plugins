import sbt._
import Keys._

import trafficland.opensource.sbt.plugins._
import releasemanagement.ReleaseManagementPlugin
import scalaconfiguration.ScalaConfigurationPlugin
import versionmanagement.VersionManagementPlugin
import trafficland.opensource.sbt.plugins.git.GitPlugin
import trafficland.opensource.sbt.plugins.packagemanagement.PackageManagementPlugin
import trafficland.opensource.sbt.plugins.distribute.DistributePlugin
import trafficland.opensource.sbt.plugins.generators.GeneratorsPlugin

object Build extends sbt.Build {

  val pluginName = "tl-sbt-plugins"
  val pluginVersion = "0.13.8-SNAPSHOT".toReleaseFormat()

  val generateKeysObject = TaskKey[Seq[File]]("generate-keys-object", "Generates the Keys Object which aliases all the plugins' keys in one object.")
  val keysFile = SettingKey[File]("keys-file", "Keys file to generate")

  val trafficlandSbtPluginProject = Project(pluginName, file("."))
    .settings(Defaults.coreDefaultSettings: _*)
    .settings(GitPlugin.plug: _*)
    .settings(PackageManagementPlugin.plug: _*)
    .settings(ReleaseManagementPlugin.plug: _*)
    .settings(ScalaConfigurationPlugin.plug: _*)
    .settings(VersionManagementPlugin.plug: _*)
    .settings(GeneratorsPlugin.plugs: _*)
    .settings(DistributePlugin.plug: _*)
    .settings(DistributePlugin.plug: _*)
    .settings(LibraryDependencies.playPlugin: _*)
    .settings(
      isApp                 := true,
      version               := pluginVersion,
      organization          := "com.trafficland",
      organizationName      := "Trafficland, Inc.",
      sbtPlugin             := true,
      scalaVersion          := "2.10.5",
      scalacOptions         := Seq("-deprecation", "-feature", "-encoding", "utf8"),
      resolvers             += "Typesafe Maven Releases" at "http://repo.typesafe.com/typesafe/releases/",
      libraryDependencies   ++= Seq(
        "org.scalatest" %% "scalatest" % "2.0.M6-SNAP36" % "test",
        "org.mockito" % "mockito-all" % "1.9.5" % "test"
      ),
      publish <<= (publish, version, streams) map { (result, appVersion, stream) =>
        appVersion.isSnapshot match {
          case false => writeReadme(appVersion, stream)
          case true  => stream.log.info("This is a snapshot; the README file does not need to be altered.")
        }
      },
      commands += distSelf,
      keysFile <<= (resourceManaged in Compile)(new File(_, "Keys.scala")),
      generateKeysObject <<= (streams, keysFile) map { (out, targetFile) =>
        out.log.info(s"Generating $targetFile")
        writeKeysObject(targetFile)
      },
      sourceGenerators in Compile <+= generateKeysObject
    )

  def writeReadme(version:String, stream:TaskStreams) {
    stream.log.info("Starting to write README.md.")
    val readmeTemplate = file("./docs/README.md.template")
    val readmeFile = file("./README.md")
    IO.write(readmeFile, IO.read(readmeTemplate).replace("#{VERSION}", version))
    stream.log.info("Finished writing README.md.")
  }

  private def writeKeysObject(targetFile: File): Seq[File] = {
    targetFile.getParentFile.mkdirs()
    val fileContents = ("bin/generate-keys" !!)
    IO.write(targetFile, fileContents)
    Seq(targetFile)
  }

  lazy val distSelf = Command.command("distSelf",
    "Compiles and packages the tl-sbt-plugins jar and then places it in project/lib",
    "Compiles and packages the tl-sbt-plugins jar and then places it in project/lib so the plugin itself can make use of its own functionality")
  { (state: State) =>
    val extracted = Project.extract(state)
    val libDir = file("project") / "lib"
    IO.delete(IO.listFiles(libDir, FileFilter.globFilter(s"$pluginName*jar")))
    IO.createDirectory(libDir)
    extracted.runTask(Keys.`package` in Compile, state) match {
      case (_, pkg) =>
        // no version name for this jar to keep source control happy without requiring to use --force
        // and remembering every time changes are made
        val fileName = s"$pluginName-self-referencing-$pluginVersion.jar"
        IO.move(pkg, libDir / fileName)
        state.log.info(s"Moved jar file to project/lib/$fileName")
      case _ =>
        state.fail
    }
    state
  }
}
