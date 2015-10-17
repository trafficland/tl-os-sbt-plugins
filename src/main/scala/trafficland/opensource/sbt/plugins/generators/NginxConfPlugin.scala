package trafficland.opensource.sbt.plugins.generators

import sbt._
import scala.collection.Seq
import java.io.File
import sbt.Keys._
import trafficland.opensource.sbt.plugins.rpm.Keys.installationDirectory

object NginxConfPlugin extends sbt.Plugin with ConfigurationDirectory with FileGenerator {

  val generateNginxConf = TaskKey[Seq[File]](
    "generate-nginx-conf",
    "destructively generates a default nginx configuration"
  )

  val nginxConfTargetFile = SettingKey[File]("nginx-conf-target-file")

  lazy val plug = DefaultConfigurationDirectory.projectSettings ++ Seq(
    nginxConfTargetFile <<= confDirectory(_ / "nginx.conf"),
    generateNginxConf <<= (streams, normalizedName, installationDirectory, nginxConfTargetFile)
     map { (out, name, installationDir, tf) =>
     val modifications = normalizedNameModification(name) ++ installationDirectoryModification(installationDir)
     generate(out, "nginx.conf.template", modifications, tf)
    }
  )

  private def installationDirectoryModification(installationDir: String): Seq[String => String] = {
    Seq[String => String](_.replace("{INSTALLATION_DIRECTORY}", installationDir))
  }
}