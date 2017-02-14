package trafficland.opensource.sbt.plugins.generators

import sbt.Keys
import java.io.File
import trafficland.opensource.sbt.plugins.utils.SourceGenerator._
import scala.Seq

trait FileGenerator {

  def generate(out: Keys.TaskStreams, templateFileName: String, modifications: Seq[String => String], targetFile: File): Seq[File] = {
    targetFile.getParentFile.mkdirs()
    val generated = fromResourceTemplate(templateFileName)(targetFile)(modifications)
    generated.foreach(f => out.log.info(s"Generated $f"))
    generated
  }

  protected def normalizedNameModification(normalizedName: String): Seq[String => String] = {
    Seq[String => String](_.replace("{NAME}", normalizedName))
  }
}
