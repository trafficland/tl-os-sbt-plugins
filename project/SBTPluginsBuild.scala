import sbt._
import Keys._
import trafficland.opensource.sbt.plugins._

object SBTPluginsBuild extends Build {

  val pluginName = "sbt-plugins"
  val pluginVersion = "0.13.0-SNAPSHOT".toReleaseFormat()

  val generateKeysObject = TaskKey[Seq[File]]("generate-keys-object", "Generates the Keys Object which aliases all the plugins' keys in one object.")
  val keysFile = SettingKey[File]("keys-file", "Keys file to generate")

  lazy val root = Project(id = "sbt-plugins", base = file("."),
    settings = StandardPluginSet.plugs ++ LibraryDependencies.playPlugin)
    .settings(
      isApp := false,
      name := "sbt-plugins",
      organization := "com.trafficland",
      organizationName := "TrafficLand, Inc.",
      sbtPlugin := true,
      version       := pluginVersion,
      scalaVersion := "2.10.3",
      scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
      resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      libraryDependencies   ++= LibraryDependencies.toSeq,
      publishTo <<= (version) { version: String =>
        val scalasbt = "http://repo.scala-sbt.org/scalasbt/"
        val (name, url) = version.isSnapshot match {
          case true => ("community-sbt-plugin-snapshots", scalasbt+"sbt-plugin-snapshots")
          case false => ("community-sbt-plugin-releases", scalasbt+"sbt-plugin-releases")
        }
        Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
      },
      publishMavenStyle := false,
      credentials += Credentials(Path.userHome / ".ivy2" / "tlcredentials" / ".scala-sbt-credentials"),
      publish <<= (publish, version, streams) map { (result, appVersion, stream) =>
        appVersion.isSnapshot match {
          case false => writeReadme(appVersion, stream)
          case true => stream.log.info("This is a snapshot.  The README file does not need to be altered.")
        }
        result
      },
      commands += distSelf,
      keysFile <<= (resourceManaged in Compile)(new File(_, "Keys.scala")),
      generateKeysObject <<= (streams, keysFile) map { (out, targetFile) =>
        out.log.info(s"Generating $targetFile")
        writeKeysObject(targetFile)
      },
      sourceGenerators in Compile <+= generateKeysObject
    )

  private def writeKeysObject(targetFile: File): Seq[File] = {
    targetFile.getParentFile.mkdirs()
    val fileContents = ("bin/generate-keys" !!)
    IO.write(targetFile, fileContents)
    Seq(targetFile)
  }

  def writeReadme(version:String, stream:TaskStreams) {
    stream.log.info("Starting to write README.md.")
    val readmeTemplate = file("./docs/README.md.template")
    val readmeFile = file("./README.md")
    IO.write(readmeFile, IO.read(readmeTemplate).replace("#{VERSION}", version))
    stream.log.info("Finished writing README.md.")
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
