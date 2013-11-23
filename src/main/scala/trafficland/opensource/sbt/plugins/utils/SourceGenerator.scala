package trafficland.opensource.sbt.plugins.utils

import sbt._
import java.io.{File, InputStream}
import scala.io.Source
import scala.annotation.tailrec

object SourceGenerator {

  def fromResourceTemplate(templateStream: InputStream, 
                           packageApplicationNamePath: File)
                          (outputDirectory: File,
                           destinationFileName: String)
                          (modifications: Seq[String => String]): Seq[File] = {
    val targetFile = outputDirectory / "main" / "scala" / packageApplicationNamePath.toString / destinationFileName
    targetFile.getParentFile.mkdirs()

    @tailrec
    def substitute(remainingModifications: Seq[String => String], accumulator: String): String = {
      remainingModifications match {
        case Nil => accumulator
        case head :: tail => substitute(tail, head(accumulator))
      }
    }
    
    val template = Source.fromInputStream(templateStream).getLines().mkString("\n")
    val modifiedTemplate = substitute(modifications, template)

    IO.write(targetFile, modifiedTemplate)

    Seq(targetFile)
  }
  
  def fromResourceTemplate(rootResourceFileName: String,
                           organization: String,
                           normalizedName: String)
                          (outputDirectory: File,
                           destinationFileName: String)
                          (modifications: Seq[String => String]): Seq[File] = {
    
    val templateStream = getClass.getResourceAsStream(s"/$rootResourceFileName")
    fromResourceTemplate(templateStream, createPackageApplicationNamePath(organization, normalizedName))(outputDirectory, destinationFileName)(modifications)
  }
  
  def sanitizeName(normalizedName: String): String = normalizedName.replace('-', '_')
  
  def createPackageApplicationNamePath(organization: String, normalizedName: String): File =
    file(organization.replace('.', Path.sep)) / sanitizeName(normalizedName)
  
}
