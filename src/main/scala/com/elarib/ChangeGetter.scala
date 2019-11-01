package com.elarib

import java.io.File

import scala.io.Source

sealed trait ChangeGetter {
  def changes: List[File]
}

object DummyChangeGetter extends ChangeGetter {

  override def changes: List[File] =
    Source
      .fromFile(new File(System.getProperty("DUMMY_CHANGE_GETTER_PATH")))
      .getLines()
      .map {
        case line if !line.isEmpty => new File(line)
      }
      .toList
}
