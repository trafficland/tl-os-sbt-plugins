package trafficland.opensource.sbt.plugins.distribute

import sbt._
import Keys._

/* adapted from https://github.com/xerial/sbt-pack/blob/develop/src/main/scala/xerial/sbt/Pack.scala */

object AllTheJarsPlugin extends Plugin {

  val allTheDependencies = taskKey[Seq[File]]("gets all of the runtime dependencies")
  val allUpdateReports = taskKey[Seq[sbt.UpdateReport]]("only for retrieving dependent module names")
  val excludeProjects = SettingKey[Seq[String]]("exclude-projects", "specify projects to exclude when packaging")
  val allLibJars = TaskKey[Seq[File]]("pack-lib-jars")
  val allUnmanagedJars = taskKey[Seq[Classpath]]("all unmanaged jar files")

  lazy val plug = Seq[Def.Setting[_]](
    excludeProjects := Seq.empty,
    allUnmanagedJars <<= (thisProjectRef, buildStructure, excludeProjects) flatMap getFromSelectedProjects(unmanagedJars.task in Compile),
    allLibJars <<= (thisProjectRef, buildStructure, excludeProjects) flatMap getFromSelectedProjects(packageBin.task in Runtime),
    allUpdateReports <<= (thisProjectRef, buildStructure, excludeProjects) flatMap getFromSelectedProjects(update.task),
    allTheDependencies := {
      val dependentJars: Seq[File] = for {
          r: sbt.UpdateReport <- allUpdateReports.value
          c <- r.configurations if c.configuration == "runtime"
          m <- c.modules
          (artifact, file) <- m.artifacts if DependencyFilter.allPass(c.configuration, m.module, artifact)}
        yield {
          file
        }

      val libs: Seq[File] = allLibJars.value

      val unmanagedJars: Seq[File] = for {
        m <- allUnmanagedJars.value
        um <- m
        f = um.data
      } yield f

      (dependentJars ++ libs ++ unmanagedJars).distinct
    }
  )

  private def getFromSelectedProjects[T](targetTask: SettingKey[Task[T]])(currentProject: ProjectRef, structure: BuildStructure, exclude: Seq[String]): Task[Seq[T]] = {
    def allProjectRefs(currentProject: ProjectRef): Seq[ProjectRef] = {
      def isExcluded(p: ProjectRef) = exclude.contains(p.project)
      val children = Project.getProject(currentProject, structure).toSeq.flatMap {
        p =>
          p.uses
      }

      (currentProject +: (children flatMap allProjectRefs)) filterNot isExcluded
    }

    val projects: Seq[ProjectRef] = allProjectRefs(currentProject).distinct
    projects.flatMap(p => targetTask in p get structure.data).join
  }

}
