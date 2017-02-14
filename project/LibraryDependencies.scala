import sbt._
import Keys._

object LibraryDependencies {

  /* modified from: https://github.com/sbt/sbt-atmos/blob/master/project/SbtAtmosBuild.scala */
  def playPlugin: Seq[Setting[_]] = Seq(
    libraryDependencies <+= (sbtVersion in sbtPlugin, scalaBinaryVersion in update) { (sbtV, scalaV) =>
      val (dependency, trimmedSbtV) = sbtV match {
        /* the typesafe ivy repo is using 0.13 instead of 0.13.0 for the sbt version in its URL */
        case "0.13.6" => ("com.typesafe.play" % "sbt-plugin" % "2.2.1", "0.13")
        case "0.13.8" => ("com.typesafe.play" % "sbt-plugin" % "2.4.0", "0.13")
        case _ => sys.error("Unsupported sbt version: " + sbtV)
      }
      Defaults.sbtPluginExtra(dependency, trimmedSbtV, scalaV)
    }
  )
}
