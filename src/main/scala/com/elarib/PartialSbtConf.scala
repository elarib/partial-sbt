package com.elarib
import sbt.{File, BuildPaths}
object PartialSbtConf {

  def metaBuildFiles(baseDir: File): Seq[(File, (File, File) => Boolean)] =
    (sbt.BuildPaths.projectStandard(baseDir).getAbsoluteFile, dirChecker) +: BuildPaths
      .configurationSources(baseDir)
      .map(file => (file.getAbsoluteFile, fileChecker))

  private val fileChecker: (File, File) => Boolean = {
    (sourceFile: File, file: File) =>
      sourceFile.getAbsoluteFile == file.getAbsoluteFile
  }

  private val dirChecker: (File, File) => Boolean = {
    (sourceFile: File, file: File) =>
      new File(
        sourceFile,
        file.getAbsolutePath.replace(sourceFile.getAbsolutePath, "")).exists()
  }

}
