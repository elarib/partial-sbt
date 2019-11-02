package com.elarib.model

import java.io.File

import scala.io.Source

sealed trait ChangeGetter {
  def changes: List[File]
}

case class GitBranchChangeGetter(sourceBranch: String, targetBranch: String)
    extends ChangeGetter {
  override def changes: List[File] = ???
}

case class GitCommitChangeGetter(sourceBranch: String, targetBranch: String)
    extends ChangeGetter {
  override def changes: List[File] = ???
}

case class DummyChangeGetter(logFilePath: String) extends ChangeGetter {

  override def changes: List[File] =
    Source
      .fromFile(new File(logFilePath))
      .getLines()
      .map {
        case line if !line.isEmpty => new File(line)
      }
      .toList
}
