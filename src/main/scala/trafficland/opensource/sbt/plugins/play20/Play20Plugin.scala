package trafficland.opensource.sbt.plugins.play20

import com.typesafe.sbt.web.Import
import sbt._
import sbt.Keys._
import trafficland.opensource.sbt.plugins.distribute.DistributePlugin
import DistributePlugin.additionalMappings
import trafficland.opensource.sbt.plugins.distribute.StartupScriptPlugin._

object Play20Plugin extends Plugin {

  lazy val plug = DistributePlugin.plug ++ Seq(
    startScriptMainClass := "play.core.server.NettyServer",
    startScriptMainArguments := Seq("$basedir"),
    additionalMappings <<= (unmanagedResourceDirectories in Import.Assets) map {
      _.flatMap {
        case dir if dir.exists() => addDirectory(dir)
        case _ ⇒ Seq.empty[(File, String)]
      }
    }
  )

  private def addDirectory(dir: File): Seq[(File, String)] = {
    def addFiles(td: File): Seq[(File, String)] = {
      td.listFiles().map {
        case f if f.isFile =>
          val relative = f.relativeTo(dir.getParentFile).get.getPath
          Seq((f, relative))
        case d if d.isDirectory => addFiles(d)
      }.flatten.toSeq
    }
    addFiles(dir)
  }
}