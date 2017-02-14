package trafficland.opensource.sbt.plugins.rpm

import sbt.SettingKey
import scala.collection.Seq
import com.typesafe.sbt.packager.linux.LinuxPackageMapping

object Keys {
  // vendor directory on target machine; defaults to /opt/$rpmVendor
  val vendorDirectory = SettingKey[String]("vendor-directory")
  // destination directory on target machine as a child of the vendor-directory setting
  val destinationDirectory = SettingKey[String]("destination-directory")
  // combination of vendor-directory and destination-directory separated by a path character
  val installationDirectory = SettingKey[String]("installation-directory")
  // the linux user to run the software under
  // the linux group to run the software under
  val linuxUserAndGroup = SettingKey[(String, String)]("linux-user-and-group")
  // additional package mappings for RPMs
  val additionalPackageMappings = SettingKey[Seq[LinuxPackageMapping]](
    "additional-package-mappings",
    "additional package mappings for use when creating an RPM"
  )
  val installedInitScriptName = SettingKey[String]("installed-init-script-name")
}
