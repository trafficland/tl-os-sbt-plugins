package trafficland.opensource.sbt.plugins.play20

import sbt._
import sbt.Keys._
import trafficland.opensource.sbt.plugins.distribute.DistributePlugin
import DistributePlugin.additionalMappings
import trafficland.opensource.sbt.plugins.distribute.StartupScriptPlugin._

object Play20Plugin extends Plugin {

  lazy val plug = DistributePlugin.plug ++ Seq(
    startScriptMainClass := "play.core.server.NettyServer",
    startScriptMainArguments := Seq("$basedir"),
    additionalMappings <<= baseDirectory map { bd =>
      bd / "public" match {
        case p if !p.exists() => Seq.empty[(File, String)]
        case public =>
          addDirectory(public, List.empty)
      }
    }
  )

  private def addDirectory(dir: File, accumulator: List[(File, String)]): Seq[(File, String)] = {
    dir.listFiles().map {
      case f @ file if !file.isDirectory =>
        val relative = f.relativeTo(dir.getParentFile).map(r => r.getPath).get
        (f, relative) :: accumulator
      case _ @ d => addDirectory(d, accumulator)
    }.flatMap { r => r}
  }

}