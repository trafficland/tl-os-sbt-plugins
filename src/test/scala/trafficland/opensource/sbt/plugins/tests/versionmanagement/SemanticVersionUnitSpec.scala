package trafficland.opensource.sbt.plugins.tests.versionmanagement

import org.scalatest.{Matchers, WordSpec}
import trafficland.opensource.sbt.plugins.versionmanagement.{InvalidSnapshotVersionFormatException, SemanticVersion}

class SemanticVersionUnitSpec extends WordSpec with Matchers {

  protected val snapshotVersion = "0.0.1-SNAPSHOT"
  protected val snapshotReleaseVersion = "0.0.1-01082013-654321"
  protected val finalVersion = "0.0.1"

  "toFinal" should {

    "strip -SNAPSHOT from the version property." in {
      val strippedVersion = SemanticVersion.stripSnapshot(snapshotVersion)

      strippedVersion should be (finalVersion)
    }

    "strip -01082013-654321 from the version property." in {
      val strippedVersion = SemanticVersion.stripSnapshot(snapshotReleaseVersion)

      strippedVersion should be (finalVersion)
    }

    "throw InvalidSnapshotVersionFormatException for release versions." in {
      evaluating { SemanticVersion.stripSnapshot(finalVersion) } should produce [InvalidSnapshotVersionFormatException]
    }
  }

  "isSnapshot" should {

    "return true for -SNAPSHOT formatted versions." in {
      SemanticVersion.isSnapshot(snapshotVersion) should be (true)
    }

    "return true for -yyyyMMdd-HHmmss formatted versions." in {
      SemanticVersion.isSnapshot(snapshotReleaseVersion) should be (true)
    }

    "return false for release versions." in {
      SemanticVersion.isSnapshot(finalVersion) should be (false)
    }

  }

  "toReleaseFormat" should {

    import trafficland.opensource.sbt.plugins._

    "return a version as a string in the snapshot release format when a version in the snapshot format is submitted." in {
      snapshotVersion.toReleaseFormat should fullyMatch regex """^(\d+\.){2}\d+(-\d{8}-\d{6})$"""
    }

    "return a version as a string in the snapshot release format when a version in the snapshot release format is submitted." in {
      snapshotReleaseVersion.toReleaseFormat should fullyMatch regex """^(\d+\.){2}\d+(-\d{8}-\d{6})$"""
    }

    "return a version in release format when a release version is submitted." in {
      finalVersion.toReleaseFormat should fullyMatch regex """^(\d+\.){2}\d+$"""
    }

    "return the same release version when a release version is submitted." in {
      val newReleaseVersion = finalVersion.toReleaseFormat()

      newReleaseVersion should equal(finalVersion)
    }

    "return the same snapshot release version when a snapshot release version is submitted." in {
      val newSnapshotReleaseVersion = snapshotReleaseVersion.toReleaseFormat()

      newSnapshotReleaseVersion should equal(snapshotReleaseVersion)
    }

  }

  "toSnapshot" should {

    import trafficland.opensource.sbt.plugins._

    "return the snapshot version of a release version." in {
      val subject = finalVersion.toSnapshot()

      subject.toString should equal(snapshotVersion)
    }
  }

  "toFinal" should {

    import trafficland.opensource.sbt.plugins._

    "return a SemanticVersion that returns the correctly formatted string when toString is called." in {
      val newFinalVersion = snapshotVersion.toFinal()

      newFinalVersion.toString() should equal(finalVersion)
    }
  }

  "toString" should {

    import trafficland.opensource.sbt.plugins._

    "return the version with the SNAPSHOT suffix when the version is a default snapshot version." in {
      val snapshotSemanticVersion : SemanticVersion = snapshotVersion

      snapshotSemanticVersion.toString should equal(snapshotVersion)
    }

    "return the version with the SNAPSHOT suffix when the version is a release snapshot version." in {
      val snapshotSemanticVersion : SemanticVersion = snapshotReleaseVersion

      snapshotSemanticVersion.toString should equal(snapshotVersion)
    }
  }

}