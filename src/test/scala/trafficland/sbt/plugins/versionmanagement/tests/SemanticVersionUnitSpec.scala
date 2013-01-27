package trafficland.sbt.plugins.versionmanagement.tests

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import trafficland.sbt.plugins.versionmanagement.{InvalidSnapshotVersionFormatException, SemanticVersion}

class SemanticVersionUnitSpec extends WordSpec with ShouldMatchers {

  protected val snapshotVersion = "0.0.1-SNAPSHOT"
  protected val snapshotReleaseVersion = "0.0.1-01082013-654321"
  protected val releaseVersion = "0.0.1"

  "stripSnapshot" should {

    "strip -SNAPSHOT from the version property." in {
      val strippedVersion = SemanticVersion.stripSnapshot(snapshotVersion)

      strippedVersion should be ("0.0.1")
    }

    "strip -01082013-654321 from the version property." in {
      val strippedVersion = SemanticVersion.stripSnapshot(snapshotReleaseVersion)

      strippedVersion should be ("0.0.1")
    }

    "throw InvalidSnapshotVersionFormatException for release versions." in {
      evaluating { SemanticVersion.stripSnapshot(releaseVersion) } should produce [InvalidSnapshotVersionFormatException]
    }
  }

  "isValidSnapshotVersionFormat" should {

    "return true for -SNAPSHOT formatted versions." in {
      SemanticVersion.isValidSnapshotVersionFormat(snapshotVersion) should be (true)
    }

    "return true for -yyyyMMdd-HHmmss formatted versions." in {
      SemanticVersion.isValidSnapshotVersionFormat(snapshotReleaseVersion) should be (true)
    }

    "return false for release versions." in {
      SemanticVersion.isValidSnapshotVersionFormat(releaseVersion) should be (false)
    }

  }

  "toReleaseFormat" should {

    import trafficland.sbt.plugins._

    "return a version as a string in the snapshot release format when a version in the snapshot format is submitted." in {
      snapshotVersion.toReleaseFormat should fullyMatch regex ("""^(\d+\.){2}\d+(-\d{8}-\d{6})$""")
    }

    "return a version as a string in the snapshot release format when a version in the snapshot release format is submitted." in {
      snapshotReleaseVersion.toReleaseFormat should fullyMatch regex ("""^(\d+\.){2}\d+(-\d{8}-\d{6})$""")
    }

    "not return the same snapshot release version when a snapshot release version is submitted." in {
      val newSnapshotReleaseVersion = snapshotReleaseVersion.toReleaseFormat

      newSnapshotReleaseVersion should not equal(snapshotReleaseVersion)
    }

    "return a version in relase format when a release version is submitted." in {
      releaseVersion.toReleaseFormat should fullyMatch regex ("""^(\d+\.){2}\d+$""")
    }

    "return the same release version when a release version is submitted." in {
      val newReleaseVersion = releaseVersion.toReleaseFormat

      newReleaseVersion should equal(releaseVersion)
    }

  }


}