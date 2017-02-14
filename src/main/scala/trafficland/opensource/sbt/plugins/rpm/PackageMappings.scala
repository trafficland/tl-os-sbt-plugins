package trafficland.opensource.sbt.plugins.rpm

import sbt._
import scala.collection._
import trafficland.opensource.sbt.plugins.utils._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.linux.{LinuxMappingDSL, LinuxPackageMapping}

object Permissions {
  val normal = "0644"
  val executable = "0755"
}

trait FileUserGroupMappings {
  def mapToUserGroup: PartialFunction[String, (String, String)]
}

trait NoSpecialUserGroupMappings extends FileUserGroupMappings {
  override def mapToUserGroup = {
    case null => ("nobody", "nobody")
  }
}

trait FileAndDirectoryMappings extends FileUserGroupMappings with LinuxMappingDSL {

  import Permissions._

  def defaultUser: String
  def defaultGroup: String

  def defaultUserGroup: PartialFunction[String, (String, String)] = PartialFunction((_) => (defaultUser, defaultGroup))

  def processZipArchive(installationDir: String, name: String, zip: sbt.File): Seq[LinuxPackageMapping] = {

    val files = mutable.ArrayBuffer.empty[LinuxPackageMapping]
    val tmpDir = IO.createTemporaryDirectory
    def substituteInstallationDirectory(f: File) =
      installationDir + f.getAbsolutePath.replace((tmpDir / name).toString, "")
    val userGroupMapping = mapToUserGroup orElse defaultUserGroup
    IO.unzip(zip, tmpDir).toList.sortBy(_.getName).foreach {
      f =>
        val destinationPath = substituteInstallationDirectory(f)
        val perms = f.getParentFile.getName match {
          case "bin" => executable
          case _ => normal
        }
      
        val (user, group) = userGroupMapping(destinationPath)
        files += packageMapping(f -> destinationPath) withUser user withGroup group withPerms perms
    }

    def recursiveDirectories(f: File): Seq[File] = {
      val these = f.listFiles(new SimpleFileFilter(_.isDirectory))
      these ++ these.filter(_.isDirectory).flatMap(recursiveDirectories)
    }

    val directories = recursiveDirectories(tmpDir / name)
      .map(substituteInstallationDirectory)
      .map(packageDirectory)

    files ++ directories
  }

  def packageDirectory(destinationDirectory: String): LinuxPackageMapping = {
    val (user, group) = (mapToUserGroup orElse defaultUserGroup)(destinationDirectory)
    packageMapping(IO.temporaryDirectory / "." -> destinationDirectory) withUser user withGroup group withPerms executable
  }
}

abstract case class DerivePackageMappings(name: String, rpmVendor: String, baseLocalDir: File, vendorDir: String,
                                          installationDir: String, defaultUser: String, defaultGroup: String,
                                          additionalPackageMappings: List[LinuxPackageMapping], installedInitScriptName: String, state: State) extends FileAndDirectoryMappings {

  def apply() : Seq[com.typesafe.sbt.packager.linux.LinuxPackageMapping] = {
    import Permissions._

    var pkgMappings = List(
      packageDirectory(vendorDir),
      packageDirectory(installationDir),
      /* do not explicitly own /var/log/$vendor/$name because that is likely a mount point in AWS */
      packageDirectory(s"/var/log/${rpmVendor}/${name}")
    ) ++ additionalPackageMappings

    if (fileExists(baseLocalDir / "scripts/init.sh")) {
      pkgMappings ++= Seq(packageMapping(baseLocalDir / "scripts/init.sh" -> s"/etc/rc.d/init.d/${installedInitScriptName}") withUser "root" withGroup "root" withPerms executable)
    }

    val extracted = Project.extract(state)
    extracted.runTask(dist, state) match {
      case (_, zip) =>
        val zipFileMappings = processZipArchive(installationDir, name, zip)
        pkgMappings ++= zipFileMappings
    }

    val pkgMappingsWithoutDS_Store = pkgMappings map { linuxPackage =>
      val filtered = linuxPackage.mappings filter {
        case (file, name) => {
          file.name != ".DS_Store"
        }
      }

      linuxPackage.copy(
        mappings = filtered,
        fileData = linuxPackage.fileData
      )
    } filter {
      linuxPackage => linuxPackage.mappings.nonEmpty
    }


    (for {
      lpm <- pkgMappingsWithoutDS_Store
      mappings <- lpm.mappings
    } yield mappings._2).sorted.foreach {
      p => state.log.info("Added to RPM spec: %s".format(p))
    }
    pkgMappingsWithoutDS_Store
  }
}




