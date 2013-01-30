package trafficland.opensource.sbt.plugins.play20

import sbt._
import sbt.Keys._

object Play20Plugin extends Plugin {

  lazy val plug = Seq(
    dist <<= distTask,
    sourceGenerators in Compile <+= (sourceManaged in Compile, name, version, organizationName) map { (outDir, appName, appVersion, orgName) =>
      writeVersion(outDir, appName, appVersion, orgName)
    }
  )

  val playPackageEverything = TaskKey[Seq[File]]("play-package-everything")
  val distDirectory = SettingKey[File]("play-dist")
  val dist = TaskKey[File]("dist", "Build the standalone application package")
  val distTask = (distDirectory, baseDirectory, playPackageEverything, dependencyClasspath in Runtime, target, normalizedName, version, name, state) map { (dist, root, packaged, dependencies, target, id, version, name, stream) =>
    val packageName = name + "-" + version
    stream.log.info("Creating package named %s.".format(packageName))
    val packageDirectory = name
    stream.log.info("Writing package to %s.".format(packageDirectory))
    val zip = dist / (packageName + ".zip")
    stream.log.info("Creating zip file %s.".format(zip.toString))

    IO.delete(dist)
    IO.createDirectory(dist)

    val dependenciesListToZipLocationMappings = getDependencyToZipLocationMappings(dependencies) ++ packaged.map(jar => jar -> ("/lib/" + jar.getName))

    val startScriptLocation = target / "start"
    val customConfigFileInformation = Option(System.getProperty("config.file"))
    val customConfigFilename = customConfigFileInformation.map(f => Some((new File(f)).getName)).getOrElse(None)

    writeStartScript(startScriptLocation, dependenciesListToZipLocationMappings, customConfigFilename)

    val scripts = Seq(startScriptLocation -> (packageDirectory + "/start"))
    val other = Seq((root / "README") -> (packageDirectory + "/README"))
    val productionConfigFile = customConfigFilename.map(fn => target / fn).getOrElse(target / "application.conf")
    val prodApplicationConf = getConfigFileToZipLocationMappings(customConfigFileInformation, productionConfigFile, packageDirectory)
    val defaultApplicationConf = Seq(new File("conf/application.conf") -> (packageDirectory + "/conf/application.conf"))

    IO.zip(dependenciesListToZipLocationMappings.map { case (jar, path) => jar -> (packageDirectory + "/" + path) } ++ scripts ++ other ++ prodApplicationConf ++ defaultApplicationConf, zip)
    IO.delete(startScriptLocation)
    IO.delete(productionConfigFile)

    println()
    println(name + " has been packaged.  The package can be found at " + zip.getCanonicalPath + "!")
    println("ready to stage")
    println()

    zip
  }

  def getConfigFileToZipLocationMappings(customConfigFileInformation:Option[String], productionConfigFile:File, packageDirectory:String) : Seq[(File, String)] = {
    customConfigFileInformation.map { location =>
      val customConfigFile = new File(location)
      IO.copyFile(customConfigFile, productionConfigFile)
      Seq(productionConfigFile -> (packageDirectory + "/conf/" + customConfigFile.getName))
    }.getOrElse(Nil)
  }

  def writeStartScript(scriptLocation:File, dependencies:Seq[(File, String)], customConfigFilename:Option[String]) {
    IO.write(scriptLocation,
      """#!/usr/bin/env sh
scriptdir=`dirname $0`
classpath=""" + dependencies.map { case (jar, path) => "$scriptdir/" + path }.mkString("\"", ":", "\"") + """
exec /opt/java $* -cp $classpath """ + customConfigFilename.map(fn => "-Dconfig.file=`dirname $0`/conf/" + fn + " ").getOrElse("-Dconfig.file=`dirname $0`/conf/application.conf ") + """play.core.server.NettyServer `dirname $0`
                                                                                                                                                                                """ /* */ )
  }

  def getDependencyToZipLocationMappings(dependencies:Id[Keys.Classpath]) : Seq[(File, String)] = dependencies.filter(_.data.ext == "jar").map { dependency =>
    val filename = for {
      module <- dependency.metadata.get(AttributeKey[ModuleID]("module-id"))
      artifact <- dependency.metadata.get(AttributeKey[Artifact]("artifact"))
    } yield {
      module.organization + "." + module.name + "-" + Option(artifact.name.replace(module.name, "")).filterNot(_.isEmpty).map(_ + "-").getOrElse("") + module.revision + ".jar"
    }
    val path = ("lib/" + filename.getOrElse(dependency.data.getName))
    dependency.data -> path
  }

  def writeVersion(outDir: File, appName:String, appVersion:String, organizationName:String) = {
    val file = outDir / "AppInfo.scala"
    IO.write(file,
      """package controllers
object AppInfo {
  val version = "%s"
  val name = "%s"
  val vendor = "%s"
}""".format(appVersion, appName, organizationName))
    Seq(file)
  }
}
