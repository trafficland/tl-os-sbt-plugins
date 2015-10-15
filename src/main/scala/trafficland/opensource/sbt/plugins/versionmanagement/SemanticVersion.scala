package trafficland.opensource.sbt.plugins.versionmanagement

import SemanticVersionConstants._

object SemanticVersionConstants {

  lazy val SNAPSHOT = "SNAPSHOT"
  lazy val HYPHENSNAPSHOT = "-%s".format(SNAPSHOT)

}

trait Snapshotable {

  def isSnapshot(version:String) = {
    version.matches("""^(\d+\.){2}\d+(-\d{8}-\d{6})$""") ||
      version.matches("""^(\d+\.){2}\d+(%s)$""".format(HYPHENSNAPSHOT))
  }

}

object SemanticVersion extends Snapshotable {

  def stripSnapshot(originalVersion:String) = {
    if (!isSnapshot(originalVersion)) throw InvalidSnapshotVersionFormatException(originalVersion)

    originalVersion endsWith (HYPHENSNAPSHOT) match {
      case false => originalVersion.replaceAll("""(-\d{8}-\d{6})""", "")
      case true => originalVersion.replaceAll(HYPHENSNAPSHOT, "")
    }
  }

  def toVersion(originalVersion:String) : SemanticVersion = {
    require(originalVersion.length > 0, "version string must be greater than 0 characters in length")
    require(originalVersion.matches("^(.+)\\.(.+)\\.(.+)"), s"""version string must contain the pattern ^(.+)\\.(.+)\\.(.+) (version string is: "$originalVersion")""")

    isSnapshot(originalVersion) match {
      case true => {
        originalVersion.split("\\.") match {
          case Array(major, minor, patch) => {
            val patchAndSnapshot = patch.split("-", 2)
            SemanticVersion(Integer.parseInt(major),
              Integer.parseInt(minor),
              Integer.parseInt(patchAndSnapshot(0)),
              patchAndSnapshot(1)
            )
          }
        }
      }
      case false => {
        originalVersion.split("\\.") match {
          case Array(major, minor, patch) => {
            SemanticVersion(Integer.parseInt(major),
              Integer.parseInt(minor),
              Integer.parseInt(patch),
              ""
            )
          }
        }
      }
    }
  }
}

case class SemanticVersion(major: Int, minor: Int, patch: Int, snapshot:String = SNAPSHOT) extends Snapshotable {

  def incPatch() = Some(copy(patch = patch + 1))

  def incMinor() = Some(copy(minor = minor + 1, patch = 0))

  def incMajor() = Some(copy(major = major + 1, minor = 0, patch = 0))

  def toFinal() = {
    copy(snapshot = "")
  }

  def toSnapshot() = copy(snapshot = SNAPSHOT)

  override def toString() = {
    val versionAsString = "%s.%s.%s".format(major, minor, patch)
    snapshot.isEmpty match {
      case true => versionAsString
      case false => versionAsString + HYPHENSNAPSHOT
    }
  }

  def toReleaseFormat() = {
    isSnapshot match {
      case true => {
        snapshot == SNAPSHOT match {
          case true => {
            import java.{util => ju}
            val sf = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss")
            sf.setTimeZone(ju.TimeZone.getTimeZone("UTC"))
            "%s.%s.%s-%s".format(major, minor, patch, sf.format(new ju.Date()))
          }
          case false => "%s.%s.%s-%s".format(major, minor, patch, snapshot)
        }
      }
      case false => toString
    }
  }

  def isSnapshot : Boolean = isSnapshot(this.toString())
}
