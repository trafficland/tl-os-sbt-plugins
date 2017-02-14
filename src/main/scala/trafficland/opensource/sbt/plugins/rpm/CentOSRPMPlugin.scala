package trafficland.opensource.sbt.plugins.rpm

import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.SbtNativePackager._
import scala.collection.Seq
import trafficland.opensource.sbt.plugins.utils.SourceGenerator._
import trafficland.opensource.sbt.plugins.rpm.Keys._

/**
 * RPM plugin with CentOS (and RHEL) specific usage of `chkconfig` and `service`.
 */
object CentOSRPMPlugin extends Plugin {

  val generateInitScript = TaskKey[Seq[File]](
    "generate-init-script",
    "destructively generates the scripts/init.sh script file for CentOS Linux system startup"
  )

  val scriptsDirectory = SettingKey[File]("scripts-directory")

  val initShTargetFile = SettingKey[File]("init-sh-target-file")

  lazy val plug = RPMPlugin.plug ++ Seq(
    rpmPost <<= (name in Rpm) apply { n => Some("chkconfig %s on".format(n)) },
    rpmPreun <<= (name, installationDirectory) apply { (n, installDir) =>
      Some(s"chkconfig $n off ; service $n stop > /dev/null 2>&1 ; rm -rf $installDir/{RUNNING_PID,*.pid} > /dev/null 2>&1")
    },
    scriptsDirectory <<= baseDirectory apply { bd => bd / "scripts" },
    initShTargetFile <<= scriptsDirectory apply { sd => sd / "init.sh" },
    generateInitScript <<= (streams, initShTargetFile, name, installationDirectory, linuxUserAndGroup) map { (out, tf, name, installDir, uag) =>
     tf.getParentFile.mkdirs()
     val generated = fromResourceTemplate("init.sh.template")(tf)(
       Seq[String => String](
         _.replace("{NAME}", name),
         _.replace("{INSTALLATION_DIRECTORY}", installDir),
         _.replace("{LINUX_USER}", uag._1)
       )
     )
     generated.foreach(f => out.log.info(s"Generated $f"))
     generated
    }
  )
}
