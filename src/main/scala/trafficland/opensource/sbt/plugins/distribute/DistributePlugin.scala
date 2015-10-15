package trafficland.opensource.sbt.plugins.distribute

import sbt._
import sbt.Keys._
import trafficland.opensource.sbt.plugins.distribute.StartupScriptPlugin._
import trafficland.opensource.sbt.plugins.distribute.AllTheJarsPlugin._
import trafficland.opensource.sbt.plugins.utils

/**
 * Making the world safe for non-Play project distribution tasks.
 */
object DistributePlugin extends Plugin {

  val StageDistribution = config("stage")

  val distDirectory = SettingKey[File]("dist-directory")
  val configFile = SettingKey[File]("config-file")
  val dist = TaskKey[File]("dist", "Build the standalone production application package as a zip archive.")
  val additionalMappings = TaskKey[Seq[(File,String)]]("additional-mappings", "Additional file to map mappings for the distribution zip archive.")

  lazy val plug = AllTheJarsPlugin.plug ++
    StartupScriptPlugin.plug ++
    Seq(
      configFile <<= baseDirectory(_ / "conf" / "prod.conf"),
      loggingConfigFileName := Some("logback.xml"),
      startScriptConfigFileName <<= configFile(_.getName)
    ) ++
    inConfig(StageDistribution)(StartupScriptPlugin.plug ++ Seq(
      configFile <<= baseDirectory(_ / "conf" / "stage.conf"),
      loggingConfigFileName := Some("logback-stage.conf"),
      startScriptConfigFileName <<= (configFile in StageDistribution)(_.getName)
    )) ++ Seq(
    distDirectory <<= baseDirectory(_ / "dist"),
    additionalMappings := Seq.empty,
    dist <<= (distDirectory, baseDirectory, target, startScript, configFile, loggingConfigFileName,
      version, normalizedName, state, additionalMappings, allTheDependencies)
      map distribution
    //    distCustom := {
    //      val args: Seq[String] = Def.spaceDelimited().parsed
    //      val customConfig = args.headOption.map(fn => file(fn))
    //
    //      customConfig match {
    //        case Some(_) => distribution(distDirectory.value, baseDirectory.value,
    //          target.value, customConfig, startScriptName.value,
    //          startScriptJavaCommand.value, startScriptJavaOptions.value, normalizedName.value, version.value,
    //          name.value, state.value, allTheDependencies.value)
    //        case None => sys.error("No configuration file specified")
    //      }
    //    }
  )

  private def distribution(distDir: File, root: File, target: File, startScript: Option[File], configFile: File,
                                loggingConfigFile: Option[String], version: String, name: String, stream: State,
                                additionalMappings: Seq[(File,String)], jars: Seq[File]): File = {

    def coalesce(pairs: Pair[File,String]*) : Seq[(File,String)] = {
      for {
        pair <- pairs if utils.fileExists(pair._1)
      } yield pair
    }

    val applicationConfigFileName = "application.conf"
    val applicationConf = root / "conf" / applicationConfigFileName

    val packageName = s"$name-$version"
    stream.log.info(s"Creating package named $packageName.")
    val packageDirectory = name
    stream.log.info(s"Writing package to $packageDirectory.")
    val zip = distDir / s"$packageName.zip"
    stream.log.info(s"Creating zip file $zip")

    IO.delete(distDir)
    IO.createDirectory(distDir)

    var common = coalesce(
      (root / "README") -> (packageDirectory + "/README"),
      (root / "README.md") -> (packageDirectory + "/README.md"),
      configFile -> (packageDirectory + s`"/conf/${configFile.getName}"),
      applicationConf -> (packageDirectory + s"/conf/$applicationConfigFileName")
    )

    loggingConfigFile.map { lcf =>
      common ++= Seq((root / "conf" / lcf) -> (packageDirectory + s"/conf/$lcf"))
    }

    val scripts = startScript match {
      case Some(ss) => Seq(ss -> (packageDirectory + s"/bin/${ss.getName}"))
      case None => Seq.empty[(java.io.File, String)]
    }

    IO.zip((jars.map(f => f -> s"$packageDirectory/lib/${f.getName}") ++
      scripts ++
      common ++
      additionalMappings.map((am) => (am._1, packageDirectory + "/" + am._2))).distinct,
      zip)

    stream.log.info("")
    stream.log.info(s"$name has been packaged; the package can be found at:")
    stream.log.info(zip.getCanonicalPath)
    stream.log.info("")

    zip
  }
}
