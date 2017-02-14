package trafficland.opensource.sbt.plugins.distribute

import sbt._
import sbt.Keys._

object StartupScriptPlugin extends Plugin {

  val startScriptFileName = SettingKey[File](
    "start-script-file-name",
    "target destination for the generated start script")

  val startScriptMainClass = SettingKey[String](
    "start-script-main-class",
    "main class executed by the script")

  val startScriptMainArguments = SettingKey[Seq[String]](
    "start-script-main-arguments",
    "arguments passed to the main class")

  val startScriptJavaCommand = SettingKey[String](
    "start-script-java-command",
    "name of the java executable")

  val startScriptJavaOptions = SettingKey[Seq[String]](
    "start-script-java-options",
    "option pairs for the java executable (with -D flag if necessary, i.e., \"-Dsome.value=1337\")")

  val startScriptConfigFileName = SettingKey[String](
    "start-script-config-file-name",
    "configuration file name passed as -Dconfig.file system setting")

  val loggingConfigFileName = SettingKey[Option[String]](
    "logging-config-file",
    "Logback configuration file in the conf directory")

  val startScript = TaskKey[Option[File]]("start-script", "the start script")

  lazy val plug = Seq(
    startScriptFileName <<= target(_ / "start"),
    startScriptJavaCommand := "java",
    startScriptJavaOptions := Seq.empty,
    startScriptMainArguments := Seq.empty,
    startScriptConfigFileName := "application.conf",
    startScriptMainClass := "Runner",
    loggingConfigFileName := Some("logback.xml"),
    startScript <<= (baseDirectory, startScriptFileName, normalizedName, startScriptMainClass, startScriptMainArguments,
      startScriptConfigFileName, loggingConfigFileName, startScriptJavaCommand, startScriptJavaOptions)
      map createStartScript
  )

  private def createStartScript(baseDirectory: File,
                       scriptLocation: File,
                       identifier: String,
                       mainClass: String,
                       mainArguments: Seq[String] = Seq.empty,
                       configFileName: String = "application.conf",
                       loggingFile: Option[String] = Some("logback.xml"),
                       javaCommand: String = "java",
                       javaOptions: Seq[String] = Seq.empty): Option[File] = {
    require(identifier != null && identifier.length > 0)
    require(mainClass != null && mainClass.length > 0)

    // both options are used here to support logback general configuration and Play specific configuration
    var modifiedJavaOptions = javaOptions
    loggingFile.map { lf =>
      modifiedJavaOptions ++= Seq(s"-Dlogback.configurationFile=$$basedir/conf/$lf",
                                  s"-Dlogger.file=$$basedir/conf/$lf")
    }

    IO.write(scriptLocation,
      """#!/bin/sh
basedir="`dirname $0`/.."
classpath="$basedir/lib/*"
exec """ + javaCommand + """ $* -cp "$classpath" """ +
        modifiedJavaOptions.mkString("", " ", " ") +
        s"-Dconfig.file=$$basedir/conf/$configFileName " +
        s"-Dinit-d-identifier=$identifier " +
        s"$mainClass " +
        mainArguments.mkString(" "))
    Some(scriptLocation)
  }
}
