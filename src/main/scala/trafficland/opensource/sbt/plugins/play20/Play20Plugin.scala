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
      val public = bd / "public"
      def addDirectory(dir: File, accumulator: List[(File, String)]): Seq[(File, String)] = {
        dir.listFiles().map(f =>
          f match {
            case file if !file.isDirectory =>
              val relative = f.relativeTo(public.getParentFile).map(r => r.getPath).get
              (f, relative) :: accumulator
            case _ @ d => addDirectory(d, accumulator)
          }
        ).flatMap { r => r }
      }

      addDirectory(public, List.empty)
    }
  )

}