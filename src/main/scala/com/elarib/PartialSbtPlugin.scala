package com.elarib

import sbt.{AutoPlugin, Command, Def}
import sbt.Keys._
import sbt._
import complete.DefaultParsers._
import org.apache.logging.log4j.LogManager
import sbt.internal.BuildDependencies.DependencyMap

object PartialSbtPlugin extends AutoPlugin {

  lazy val logger = {
    val context = LogManager
      .getContext(
        this.getClass.getClassLoader,
        false,
        sys.props
          .get("log4j.configurationFile") match {
          case Some(value) => new File(value).toURI

          case None =>
            val value = getClass.getResource("/log4j2.properties")
            System.setProperty("log4j.configurationFile", value.getPath)
            value.toURI
        }
      )
    context.getLogger(getClass.getName)
  }

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      commands += Command("metaBuildChangedFiles")(_ => EOF)((st, _) => {
        val metaBuildChangedFiles =
          getMetaBuildChangedFiles(DummyChangeGetter)(baseDirectory.value)

        logger.debug(
          s"${metaBuildChangedFiles.size} meta build files have been changed.")

        metaBuildChangedFiles.foreach { file =>
          logger.debug(file)
        }
        st
      }),
      commands += Command("changedProjects")(_ => EOF)((st, _) => {
        val changedProjects: Seq[ResolvedProject] =
          findChangedModules(DummyChangeGetter)(
            baseDirectory.value,
            loadedBuild.value.allProjectRefs,
            buildDependencies.value.classpathTransitive)

        logger.debug(s"${changedProjects.size} projects have been changed")

        changedProjects.foreach { resolvedProject =>
          logger.debug(resolvedProject.id)
        }
        st
      })
    )

  private def findChangedModules(changeGetter: ChangeGetter)(
      baseDir: File,
      allProjectRefs: Seq[(ProjectRef, ResolvedProject)],
      buildDeps: DependencyMap[ProjectRef]
  ): Seq[ResolvedProject] = {

    val projectMap: Map[ProjectRef, ResolvedProject] = allProjectRefs.toMap

    val reverseDependencyMap: DependencyMap[ResolvedProject] = buildDeps
      .foldLeft[DependencyMap[ResolvedProject]](Map.empty) {
        (acc, dependency) =>
          val (ref, dependsOnList) = dependency

          dependsOnList.foldLeft(acc) { (dependencyMap, key) =>
            val resolvedProjects = dependencyMap.getOrElse(key, Nil)
            val newValue: Seq[ResolvedProject] =
              projectMap.get(ref).fold(resolvedProjects)(_ +: resolvedProjects)
            dependencyMap + (key -> newValue)
          }

      }

    val modulesWithPath: Seq[(ProjectRef, ResolvedProject)] =
      allProjectRefs.filter(_._2.base != baseDir)

    val diffsFiles: Seq[sbt.File] = changeGetter.changes

    val modulesToBuild: Seq[ResolvedProject] = modulesWithPath
      .filter {
        case (_, resolvedProject) =>
          !diffsFiles
            .filter(file =>
              file.getAbsolutePath.contains(
                resolvedProject.base.getAbsolutePath))
            .isEmpty
      }
      .flatMap {
        case (projectRef, resolvedProject) =>
          reverseDependencyMap
            .get(projectRef)
            .map(_ :+ resolvedProject)
            .getOrElse(Seq(resolvedProject))
      }
      .distinct
      .sortBy(_.id)

    modulesToBuild
  }

  private def getMetaBuildChangedFiles(changeGetter: ChangeGetter)(
      baseDir: File): List[File] = {

    lazy val metaBuildFiles: Seq[(File, (File, File) => Boolean)] =
      PartialSbtConf.metaBuildFiles(baseDir)

    for {
      fileChanged <- changeGetter.changes.flatMap(_.relativeTo(baseDir))
      (metaFile, metaFileChecker) <- metaBuildFiles.flatMap {
        case (metaFile, metaFileChecker) =>
          metaFile.relativeTo(baseDir).map((_, metaFileChecker))
      }
      if metaFileChecker(metaFile, fileChanged)
    } yield fileChanged

  }

}
