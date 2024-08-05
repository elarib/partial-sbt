package com.elarib

import com.elarib.model.{ChangeGetter, PartialSbParser}
import org.apache.logging.log4j.LogManager
import sbt.*
import sbt.Keys.*
import sbt.internal.BuildDependencies.DependencyMap

object BuildKeys {
  val partialSbtExcludedFiles = sbt.settingKey[Seq[sbt.File]]("Files that should be excluded from analysis.")
  val partialSbtExcludedProject: SettingKey[Unit] = sbt.settingKey[Unit]("Exclude project from analysis.")
}

object PartialSbtPlugin extends AutoPlugin {

  private[elarib] lazy val logger = {
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

  override def globalSettings: Seq[Def.Setting[_]] =
    Seq(
      BuildKeys.partialSbtExcludedFiles := Def.setting(List.empty[sbt.File]).value
    )

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      commands += Command("metaBuildChangedFiles")(_ => PartialSbParser.changeGetterParser)((st, changeGetter) => {
        val metaBuildChangedFiles =
          getMetaBuildChangedFiles(changeGetter)(baseDirectory.value, BuildKeys.partialSbtExcludedFiles.value)

        logger.debug(s"${metaBuildChangedFiles.size} meta build files have been changed.")

        metaBuildChangedFiles.foreach { file =>
          logger.debug(file)
        }
        st
      }),
      commands += Command("changedProjects")(_ => PartialSbParser.changeGetterParser)((st, changeGetter) => {

        val changedProjects: Seq[ResolvedProject] =
          findChangedModules(changeGetter)(
            baseDirectory.value,
            loadedBuild.value.allProjectRefs,
            buildDependencies.value.classpathTransitive,
            BuildKeys.partialSbtExcludedFiles.value
          )

        logger.debug(s"${changedProjects.size} projects have been changed")

        changedProjects.foreach { resolvedProject =>
          logger.debug(resolvedProject.id)
        }
        st
      })
    )

  private def findChangedModules(changeGetter: ChangeGetter)(
      baseDir: sbt.File,
      allProjectRefs: Seq[(ProjectRef, ResolvedProject)],
      buildDeps: DependencyMap[ProjectRef],
      excludedFiles: Seq[sbt.File]
  ): Seq[ResolvedProject] = {

    val projectMap: Map[ProjectRef, ResolvedProject] = allProjectRefs.toMap

    getMetaBuildChangedFiles(changeGetter)(baseDir, excludedFiles) match {
      case _ :: _ =>
        logger.debug(s"Metabuild files have changed. Need to reload all the ${projectMap.size} projects")
        projectMap.values.toSeq
          .sortBy(_.id)
      case Nil =>
        val reverseDependencyMap: DependencyMap[ResolvedProject] = buildDeps
          .foldLeft[DependencyMap[ResolvedProject]](Map.empty) { (acc, dependency) =>
            val (ref, dependsOnList) = dependency

            dependsOnList.foldLeft(acc) { (dependencyMap, key) =>
              val resolvedProjects = dependencyMap.getOrElse(key, Nil)
              val newValue: Seq[ResolvedProject] =
                projectMap
                  .get(ref)
                  .fold(resolvedProjects)(_ +: resolvedProjects)
              dependencyMap + (key -> newValue)
            }

          }

        val modulesWithPath: Seq[(ProjectRef, ResolvedProject)] =
          allProjectRefs.filter(_._2.base != baseDir)

        val diffsFiles: Seq[sbt.File] = changeGetter.changes.filterNot(f => isFileExcluded(baseDir)(f, excludedFiles))

        val modulesToBuild: Seq[ResolvedProject] = modulesWithPath
          .filter { case (_, resolvedProject) =>
            val projectIsIncludedInAnalysis = !resolvedProject.settings.exists(_.key.key == BuildKeys.partialSbtExcludedProject.key)
            val projectFilesWereChanged = diffsFiles.exists(file => file.getAbsolutePath.contains(resolvedProject.base.getAbsolutePath))
            projectIsIncludedInAnalysis && projectFilesWereChanged
          }
          .flatMap { case (projectRef, resolvedProject) =>
            reverseDependencyMap
              .get(projectRef)
              .map(_ :+ resolvedProject)
              .getOrElse(Seq(resolvedProject))
          }
          .distinct
          .sortBy(_.id)

        modulesToBuild
    }

  }

  private def getMetaBuildChangedFiles(
      changeGetter: ChangeGetter
  )(baseDir: sbt.File, excludedFiles: Seq[sbt.File]): List[sbt.File] = {

    lazy val metaBuildFiles: Seq[(sbt.File, (sbt.File, sbt.File) => Boolean)] =
      PartialSbtConf.metaBuildFiles(baseDir)

    for {
      fileChanged <- changeGetter.changes
        .flatMap(_.relativeTo(baseDir))
        .filterNot(f => isFileExcluded(baseDir)(f, excludedFiles))
      (metaFile, metaFileChecker) <- metaBuildFiles.flatMap { case (metaFile, metaFileChecker) =>
        metaFile.relativeTo(baseDir).map((_, metaFileChecker))
      }
      if metaFileChecker(metaFile, fileChanged)
    } yield fileChanged

  }

  private def isFileExcluded(baseDir: sbt.File)(file: sbt.File, toExclude: Seq[sbt.File]): Boolean =
    toExclude.exists {
      case ef if ef.isAbsolute =>
        (for {
          efRel <- ef.relativeTo(baseDir)
          fRel <- if (file.isAbsolute) file.relativeTo(baseDir) else Some(file)
        } yield efRel.getCanonicalPath == fRel.getCanonicalPath).getOrElse(false)
      case ef =>
        if (file.isAbsolute)
          file.relativeTo(baseDir).exists(_.getCanonicalPath == ef.getCanonicalPath)
        else
          file.getCanonicalPath == ef.getCanonicalPath
    }

}
