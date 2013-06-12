package trafficland.opensource.sbt.plugins.versionmanagement

import sbt._
import Keys._
import java.util.regex.Pattern
import trafficland.opensource.sbt.plugins._

object VersionManagementPlugin extends Plugin {

  lazy val plug = Seq(
    commands ++= Seq(
      versionBumpMajor,
      versionBumpMinor,
      versionBumpPatch,
      versionToSnapshot,
      versionToFinal,
      versionSet,
      versionWriteSnapshotRelease
    ),
    versionSettingRegexes := versionRegexes
  )

  lazy val versionRegexes = Seq("""\bappVersion\s+=\s*("(.*?)")""", """\bversion\s+:=\s*("(.*?)")""")
  lazy val versionPatterns = versionRegexes.map(Pattern.compile(_))

  val versionSettingRegexes = SettingKey[Seq[String]]("version-setting-regexes", "a list of regexes to use to replace versions")

  def versionBumpMajor = Command.command(
    "version-bump-major",
    "Bump the major version number (for example, 2.1.4 -> 3.0.0)",
    ""
  ) { state => changeVersion(state, BumpMajorChangeRequest(state)) }

  def versionBumpMinor = Command.command(
    "version-bump-minor",
    "Bump the minor version number (for example, 2.1.4 -> 2.2.0)",
    ""
  ) { state => changeVersion(state, BumpMinorChangeRequest(state)) }

  def versionBumpPatch = Command.command(
    "version-bump-patch",
    "Bump the patch version number (for example, 2.1.4 -> 2.1.5)",
    ""
  ) { state => changeVersion(state, BumpPatchChangeRequest(state)) }

  def versionToSnapshot = Command.command(
    "version-to-snapshot",
    "Convert the current version into a snapshot release (for example, 2.1.4 -> 2.1.4-SNAPSHOT)",
    ""
  ) { state => changeVersion(state, ToSnapshotChangeRequest(state)) }

  def versionToFinal = Command.command(
    "version-to-final",
    "Convert the current version into a final release version (for example, 2.1.4-SNAPSHOT -> 2.1.4)",
    ""
  ) { state => changeVersion(state, ToFinalChangeRequest(state)) }

  def versionSet = Command.single(
    "version-set",
    ("version-set version", "Manually set the current version"),
    ""
  ) { (state: State, versionToSet: String) => changeVersion(state, SetVersionChangeRequest(state, versionToSet)) }

  def versionWriteSnapshotRelease = Command.command(
    "version-write-snapshot-release",
    "Writes the release format of the snapshot version.  This is used to preserve the actual snapshot version in a release commit.",
    ""
  ) { state => changeVersion(state, SnapshotReleaseChangeRequest(state)) }

  protected def changeVersion(state:State, changeRequest:VersionChangeRequest) : State = {
    val extractedState = Project.extract(state)
    val buildBaseDirectory = extractedState.get(Keys.baseDirectory)
    val versionRegexes = extractedState.get(VersionManagementPlugin.versionSettingRegexes)

    state.log.info("Original version: %s".format(changeRequest.originalVersion.toString))
    state.log.info("New version: %s".format(changeRequest.newVersionFormatToWrite))

    val files = (PathFinder(buildBaseDirectory / "project") ** "*.scala").get ++
      (PathFinder(buildBaseDirectory / ".." / "project") ** "*.scala").get ++
      Seq((buildBaseDirectory / "build.sbt")) ++
      Seq((buildBaseDirectory / ".." / "build.sbt"))

    val matchers = versionRegexes.map(Pattern.compile(_))
    files.filter(_.exists).headOption.foreach { file =>
      state.log.info("Setting version %s in file %s".format(changeRequest.newVersionFormatToWrite, file))
      writeNewVersion(file, matchers, changeRequest)
    }

    State.stateOps(state).reload
  }

  protected def writeNewVersion(file: File, matchers: Seq[Pattern], changeRequest:VersionChangeRequest) {
    IO.writeLines(file, transformLines(IO.readLines(file), changeRequest.originalVersion, changeRequest.newVersionFormatToWrite))
  }

  def transformLines(originalSequenceOfLines:Seq[String], originalVersion:SemanticVersion, newVersionFormatToWrite:String) : Seq[String] = {
    originalSequenceOfLines.map { line =>
      searchForVersionPattern(line, originalVersion) match {
        case Some(versionFormatFound) => line.replaceAll(versionFormatFound, newVersionFormatToWrite)
        case None => line
      }
    }
  }

  def searchForVersionPattern(line:String, versionOfInterest:SemanticVersion) : Option[String] = {
    versionPatterns.map{
      pattern => pattern.matcher(line).find() match {
        case true => {
          Seq(versionOfInterest.toString, versionOfInterest.toReleaseFormat())
            .map(versionFormat => line.contains(versionFormat) match { case true => Some(versionFormat); case false => None })
            .filter(_.isDefined)
            .headOption
            .getOrElse(None)
        }
        case false => None
      }
    }.filter(_.isDefined)
    .headOption
    .getOrElse(None)
  }

  abstract class VersionChangeRequest(state:State) {

    lazy val originalVersion : SemanticVersion = {
      val extractedState = Project.extract(state)
      extractedState.get(Keys.version)
    }

    val newVersion : SemanticVersion

    lazy val newVersionFormatToWrite = newVersion.toString()
  }

  case class BumpMajorChangeRequest(state:State) extends VersionChangeRequest(state) {

    override val newVersion : SemanticVersion = originalVersion.incMajor().get
  }

  case class BumpMinorChangeRequest(state:State) extends VersionChangeRequest(state) {

    override val newVersion : SemanticVersion = originalVersion.incMinor().get
  }

  case class BumpPatchChangeRequest(state:State) extends VersionChangeRequest(state) {

    override val newVersion : SemanticVersion = originalVersion.incPatch().get
  }

  case class ToSnapshotChangeRequest(state:State) extends VersionChangeRequest(state) {

    override val newVersion : SemanticVersion = originalVersion.toSnapshot()
  }

  case class ToFinalChangeRequest(state:State) extends VersionChangeRequest(state) {

    override val newVersion : SemanticVersion = originalVersion.toFinal()
  }

  case class SetVersionChangeRequest(state:State, setToVersion:String) extends VersionChangeRequest(state) {

    override val newVersion : SemanticVersion = setToVersion
    override lazy val newVersionFormatToWrite : String = newVersion.toReleaseFormat()
  }

  case class SnapshotReleaseChangeRequest(state:State) extends VersionChangeRequest(state) {

    override val newVersion : SemanticVersion = originalVersion
    override lazy val newVersionFormatToWrite : String = if (!originalVersion.isSnapshot) throw InvalidSnapshotVersionFormatException(originalVersion.toString()); else newVersion.toReleaseFormat()
  }
}

