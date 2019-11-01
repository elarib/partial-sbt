package com.elarib

import sbt.{AutoPlugin, Command, Def}
import sbt.Keys._
import sbt._
import complete.DefaultParsers._
import org.apache.logging.log4j.LogManager

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
      commands += Command("partialCommand")(_ => EOF)((st, _) => {
        projectID.value
        st
      })
    )

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
