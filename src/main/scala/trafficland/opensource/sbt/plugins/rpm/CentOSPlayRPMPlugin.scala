package trafficland.opensource.sbt.plugins.rpm

import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager.Keys._
import trafficland.opensource.sbt.plugins.rpm.Keys._
import com.typesafe.sbt.SbtNativePackager._

object CentOSPlayRPMPlugin extends Plugin {

  val wwwUserAndGroup = SettingKey[(String, String)]("www-user-and-group", "user and group that is assigned to Play specific directories")

  lazy val plug = CentOSRPMPlugin.plug ++ Seq(
    wwwUserAndGroup := ("nginx", "nginx"),

    rpmPretrans := Some("""if [ -e /etc/nginx/conf.d/default.conf ]; then
                           | mv /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf.orig
                           | fi""".stripMargin),

    linuxPackageMappings <<= (name in Rpm, rpmVendor in Rpm, baseDirectory, vendorDirectory, installationDirectory, linuxUserAndGroup, additionalPackageMappings, installedInitScriptName, state, wwwUserAndGroup)
     map { (name, vendor, baseDir, vendorDir, installDir, uag, addlMappings, script, state, wwwUAG) =>
     val f = new DerivePackageMappings(name, vendor, baseDir, vendorDir, installDir, uag._1, uag._2, addlMappings.toList, script, state) {
       override def mapToUserGroup = {
         case s if s.contains("public/") => (wwwUAG._1, wwwUAG._2)
       }
     }
     f.apply()
    }
  )
}
