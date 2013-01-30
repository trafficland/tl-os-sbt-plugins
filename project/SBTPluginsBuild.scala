import sbt._
import Keys._
import trafficland.opensource.sbt.plugins._

object SBTPluginsBuild extends Build {

  lazy val root = Project(id = "sbt-plugins", base = file("."),
    settings = StandardPluginSet.plugs ++
    Seq(
      isApp := false,
      name := "sbt-plugins",
      organization := "com.trafficland",
      organizationName := "TrafficLand, Inc.",
      sbtPlugin := true,
      version       := "0.6.4".toReleaseFormat,
      scalaVersion := "2.9.2",
      scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
      resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "1.8" % "test"
      ),
      publishTo <<= (version) { version: String =>
        val tlArtifactoryServer = "http://build01.tl.com:8081/artifactory/"
        val repositoryPath = version.isSnapshot match {
          case true => "com.trafficland.snapshots"
          case false => "com.trafficland.final"
        }
        Some(Resolver.url("Artifactory Realm", new URL(tlArtifactoryServer + repositoryPath))((Resolver.ivyStylePatterns)))
      },
      publishMavenStyle := false,
      credentials += Credentials(Path.userHome / ".ivy2" / "tlcredentials" / ".credentials")
    )
  )
}
