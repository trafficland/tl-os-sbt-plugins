package trafficland.sbt.plugins.versionmanagement

import sbt._
import Keys._
import java.util.regex.Pattern
import trafficland.sbt.plugins._

object VersionManagementPlugin extends Plugin {

  lazy val plug = Seq(
    commands ++= Seq(
      versionBumpMajor,
      versionBumpMinor,
      versionBumpPatch,
      versionToSnapshot,
      versionToStable,
      versionSet
    ),
    versionSettingRegexes := Seq("""\bappVersion\s+=\s*("(.*?)")""", """\bversion\s+:=\s*("(.*?)")""")
  )

  val versionSettingRegexes = SettingKey[Seq[String]]("version-setting-regexes", "a list of regexes to use to replace versions")

  def versionBumpMajor = Command.command(
    "version-bump-major",
    "Bump the major version number (for example, 2.1.4 -> 3.0.0)",
    ""
  ) { state => changeVersion(state) { _.incMajor } }

  def versionBumpMinor = Command.command(
    "version-bump-minor",
    "Bump the minor version number (for example, 2.1.4 -> 2.2.0)",
    ""
  ) { state => changeVersion(state) { _.incMinor } }

  def versionBumpPatch = Command.command(
    "version-bump-patch",
    "Bump the patch version number (for example, 2.1.4 -> 2.1.5)",
    ""
  ) { state => changeVersion(state) { _.incPatch } }

  def versionToSnapshot = Command.command(
    "version-to-snapshot",
    "Convert the current version into a snapshot release (for example, 2.1.4 -> 2.1.4-SNAPSHOT)",
    ""
  ) { state => changeVersion(state) { v => Some(v.toSnapshot) } }

  def versionToStable = Command.command(
    "version-to-stable",
    "Convert the current version into a stable release (for example, 2.1.4-SNAPSHOT -> 2.1.4)",
    ""
  ) { state => changeVersion(state) { v => Some(v.stripSnapshot) } }

  def versionSet = Command.single(
    "version-set",
    ("version-set version", "Manually set the current version"),
    ""
  ) { (state: State, v: String) => changeVersion(state) { old => Some(v) } }

  def changeVersion(state: State)(versionTransform: SemanticVersion => Option[SemanticVersion]): State = {
    val extractedState = Project.extract(state)
    val buildBaseDirectory = extractedState.get(Keys.baseDirectory)
    val versionRegexes = extractedState.get(VersionManagementPlugin.versionSettingRegexes)
    val originalVersion = extractedState.get(Keys.version)
    versionTransform(originalVersion) match {
      case Some(to) => {
        val files = (PathFinder(buildBaseDirectory / "project") ** "*.scala").get ++
          Seq((buildBaseDirectory / "build.sbt")) ++
          Seq((buildBaseDirectory / ".." / "build.sbt"))
        val matchers = versionRegexes.map(Pattern.compile(_))
        files.filter(_.exists).headOption.foreach { f =>
          state.log.info("Setting version %s in file %s".format(to, f))
          writeNewVersion(f, matchers, originalVersion, to)
        }
      }
      case _ => {
        state.log.warn("Version %s is not a semantic version, cannot change".format(originalVersion))
      }
    }
    State.stateOps(state).reload
  }

  def writeNewVersion(f: File, matchers: Seq[Pattern], from: SemanticVersion, to: SemanticVersion) {
    var shouldWrite = false
    val newLines = IO.reader(f) { reader =>
      IO.foldLines(reader, Seq[String]()) { (lines, line) =>
        lines :+ matchers.foldLeft(line) { (line, r) =>
          val verMatcher = r.matcher(line)
          if (verMatcher.find() && line.contains(from.toString)) {
            shouldWrite = true
            line.replaceAll(from.toString, to.toString)
          } else {
            line
          }
        }
      }
    }
    if (shouldWrite) {
      IO.writeLines(f, newLines)
    }
  }
}
