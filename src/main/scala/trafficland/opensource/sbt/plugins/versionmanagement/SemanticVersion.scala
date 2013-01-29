package trafficland.opensource.sbt.plugins.versionmanagement

object SemanticVersion {

  def stripSnapshot(originalVersion:String) = {
    if (!isValidSnapshotVersionFormat(originalVersion)) throw InvalidSnapshotVersionFormatException(originalVersion)

    originalVersion endsWith ("-SNAPSHOT") match {
      case false => originalVersion.replaceAll("""(-\d{8}-\d{6})""", "")
      case true => originalVersion.replaceAll("-SNAPSHOT", "")
    }
  }

  def isSnapshot(version:String) = isValidSnapshotVersionFormat(version)

  def isValidSnapshotVersionFormat(version:String) = {
    version.matches("""^(\d+\.){2}\d+(-\d{8}-\d{6})$""") ||
      version.matches("""^(\d+\.){2}\d+(-SNAPSHOT)$""")
  }

  def toVersion(originalVersion:String) : SemanticVersion = {
    isSnapshot(originalVersion) match {
      case true => {
        stripSnapshot(originalVersion).split("\\.") match {
          case Array(major, minor, patch) => {
            SemanticVersion(Integer.parseInt(major),
              Integer.parseInt(minor),
              Integer.parseInt(patch),
              true
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
              false
            )
          }
        }
      }
    }
  }
}

case class SemanticVersion(major: Int, minor: Int, patch: Int, isSnapshot: Boolean) {

  def incPatch() = Some(copy(patch = patch + 1))

  def incMinor() = Some(copy(minor = minor + 1, patch = 0))

  def incMajor() = Some(copy(major = major + 1, minor = 0, patch = 0))

  def stripSnapshot() = {
    copy(isSnapshot = false)
  }

  def toSnapshot() = copy(isSnapshot = true)

  override def toString() = {
    val versionAsString = "%s.%s.%s".format(major, minor, patch)
    isSnapshot match {
      case true => versionAsString + "-SNAPSHOT"
      case false => versionAsString
    }
  }

  def toReleaseFormat() = {
    isSnapshot match {
      case true => {
        import java.{util => ju}
        val sf = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss")
        sf.setTimeZone(ju.TimeZone.getTimeZone("UTC"))
        "%s.%s.%s-%s".format(major, minor, patch, sf.format(new ju.Date()))
      }
      case false => toString
    }
  }
}