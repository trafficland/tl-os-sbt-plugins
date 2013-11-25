import sbt._
import Keys._

object LibraryDependencies {

  object Organizations {
    val scalaTest     = "org.scalatest"
    val mockito       = "org.mockito"
    val play          = "com.typesafe.play"
  }

  object Names {
    val scalaTest     = "scalatest"
    val mockito       = "mockito-core"
    val playSbtPlugin = "sbt-plugin"
  }

  object Versions {
    val scalaTest     = "2.0"
    val mockito       = "1.9.5"
    val playSbtPlugin = "2.2.1"
  }

  /* modified from: https://github.com/sbt/sbt-atmos/blob/master/project/SbtAtmosBuild.scala */
  def playPlugin: Seq[Setting[_]] = Seq(
    libraryDependencies <+= (sbtVersion in sbtPlugin, scalaBinaryVersion in update) { (sbtV, scalaV) =>
      val (dependency, trimmedSbtV) = sbtV match {
        /* the typesafe ivy repo is using 0.13 instead of 0.13.0 for the sbt version in its URL */
        case "0.13.0" => ("com.typesafe.play" % "sbt-plugin" % "2.2.1", "0.13")
        case _ => sys.error("Unsupported sbt version: " + sbtV)
      }
      Defaults.sbtPluginExtra(dependency, trimmedSbtV, scalaV)
    }
  )

  def toSeq: Seq[ModuleID] = Seq(
    Organizations.scalaTest %% Names.scalaTest      % Versions.scalaTest      % "test",
    Organizations.mockito   %  Names.mockito        % Versions.mockito        % "test"
  )
}
