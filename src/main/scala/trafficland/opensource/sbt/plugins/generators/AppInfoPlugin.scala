package trafficland.opensource.sbt.plugins.generators

import sbt._
import sbt.Keys._
import java.io.FileWriter
import java.util.Properties
import trafficland.opensource.sbt.plugins.utils.SourceGenerator._

object AppInfoPlugin extends Plugin {

  val appInfoPropertiesFileName = SettingKey[String](
    "app-info-properties-file-name",
    "filename used to build the appInfoPropertiesFile setting"
  )
  val appInfoClassFileName = "AppInfo.scala"

  val appInfoPropertiesWrite = TaskKey[Seq[File]](
    "app-info-properties-write",
    "writes the application properties file in managed resources"
  )

  val appInfoPropertiesFile = SettingKey[File](
    "app-info-properties-file",
    "path to the appinfo.properties file (will end up as a child under managed resources)"
  )

  val generateAppInfoClass = TaskKey[Seq[File]](
    "generate-app-info-class",
    "generates the AppInfo.scala file"
  )

  lazy val plug = Seq(
    appInfoPropertiesFileName <<= name { n => s"$n-appinfo.properties" },
    appInfoPropertiesFile <<= (resourceManaged in Compile, appInfoPropertiesFileName) { (d, fn) => new File(d, fn) },
    appInfoPropertiesWrite <<= (streams, appInfoPropertiesFile, name, version, organizationName) map {
      (out, targetFile, appName, appVersion, orgName) =>
        out.log.info(s"Writing app info properties to $targetFile")
        writeAppInfoProperties(targetFile, appName, appVersion, orgName)
    },
    resourceGenerators in Compile <+= appInfoPropertiesWrite,
    generateAppInfoClass <<= (streams, normalizedName, version, organization, baseDirectory, appInfoPropertiesFile) map { (out, name, v, org, dir, file) =>
      val generated = fromResourceTemplate(s"$appInfoClassFileName.template", org, name)(dir / "src", appInfoClassFileName)(
        Seq[String => String](
          _.replace("{PACKAGE}", s"$org.${sanitizeName(name)}"),
          _.replace("{APPINFOPROPERTIES}", file.getName)
        )
      )
      generated.foreach(f => out.log.info(s"Generated $f"))
      generated
    }
  )

  def writeAppInfoProperties(targetFile: File, name: String, version: String, organizationName: String): Seq[File] = {
    val appInfoProperties = new Properties
    appInfoProperties.setProperty("name", name)
    appInfoProperties.setProperty("version", version)
    appInfoProperties.setProperty("vendor", organizationName)

    targetFile.getParentFile.mkdirs() /* main directory not necessarily created yet */
    val fileWriter = new FileWriter(targetFile)
    appInfoProperties.store(fileWriter, "")
    fileWriter.close()

    Seq(targetFile)
  }
}
