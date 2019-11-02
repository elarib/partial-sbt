package com.elarib.model
import sbt.internal.util.complete._
import Parser._

object PartialSbParser extends Parsers {

  def changeGetterParseer: Parser[ChangeGetter] =
    (' ' ~ ("gitBranch" | "gitCommit") ~ ' ' ~ NotQuoted ~ ' ' ~ NotQuoted)
      .map {
        case (((((_, changeGetterName), _), firstParm), _), secondParam) =>
          changeGetterName match {
            case "gitBranch" => GitBranchChangeGetter(firstParm, secondParam)
            case "gitCommit" => GitCommitChangeGetter(firstParm, secondParam)
          }
      } | (' ' ~ "dummyChanges" ~ ' ' ~ NotQuoted).map {
      case (((_, _), _), logFilePath) => DummyChangeGetter(logFilePath)
    }
}
