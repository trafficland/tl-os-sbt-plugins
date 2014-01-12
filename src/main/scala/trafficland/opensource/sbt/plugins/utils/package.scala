package trafficland.opensource.sbt.plugins

import sbt._
import java.nio.file.{Paths, Files}

package object utils {
  def fileExists(file: File): Boolean = Files.exists(Paths.get(file.toString))
}
