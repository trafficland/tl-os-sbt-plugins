package trafficland.sbt.plugins.versionmanagement

case class InvalidSnapshotVersionFormatException(originalVersion:String)
  extends Exception(("%s is not in the correct isSnapshot version format.  " +
    "Versions must specified in one of the following formats: " +
    "major.minor.patch-SNAPSHOT, " +
    "major.minor.patch-yyyyMMdd-HHmmss").format(originalVersion)) { }
