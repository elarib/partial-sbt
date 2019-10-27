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
      commands += Command("partialCommand")(
        _ => spaceDelimited("<arg>")
      )((st, args) => {
        projectID.value.name
        st
      })
    )

}
