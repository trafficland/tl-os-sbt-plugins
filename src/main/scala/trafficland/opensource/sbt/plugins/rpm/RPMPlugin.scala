package trafficland.opensource.sbt.plugins.rpm

import com.typesafe.sbt.packager.rpm.RpmKeys
import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager.linux.{LinuxPackageMapping, LinuxSymlink}
import scala.collection._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.SbtNativePackager._
import trafficland.opensource.sbt.plugins.rpm.Keys._
import com.typesafe.sbt.SbtNativePackager

object RPMPlugin extends Plugin with RpmKeys {

  lazy val plug = SbtNativePackager.projectSettings ++
    Seq(
      additionalPackageMappings := Seq.empty[LinuxPackageMapping],
      name in Rpm <<= name apply { n => n },
      installedInitScriptName <<= name apply { n => n },

       linuxPackageMappings <<= (name in Rpm, rpmVendor in Rpm, baseDirectory, vendorDirectory, installationDirectory, linuxUserAndGroup, additionalPackageMappings, installedInitScriptName, state)
         map { (name, vendor, baseDir, vendorDir, installDir, uag, addlMappings, initScriptTargetName, state) =>
           val f = new DerivePackageMappings(name, vendor, baseDir, vendorDir, installDir, uag._1, uag._2, addlMappings.toList, initScriptTargetName, state) with NoSpecialUserGroupMappings
           f.apply()
       },

       linuxPackageSymlinks <<= (installationDirectory, rpmVendor, name in Rpm) map { (installationDir, v, n) =>
           val vendorName = s"$v/$n"
           Seq(
             LinuxSymlink(s"$installationDir/logs", s"/var/log/$vendorName"),
             LinuxSymlink(s"/etc/$vendorName", s"$installationDir/conf")
           )
       },

      version in Rpm <<= version apply { v => v.replace("-", "") },
      rpmLicense := Some("Proprietary"),
      rpmGroup := Some("Applications/Services"),
      rpmRelease := "1",
      linuxUserAndGroup := ("nobody", "nobody"),
      vendorDirectory <<= rpmVendor apply { rv => "/opt/" + rv },
      installationDirectory <<= (vendorDirectory, destinationDirectory) apply { (vd, dd) => vd + Path.sep + dd },
      destinationDirectory <<= (name in Rpm) apply { n => n }
    )
}