package com.elarib.model

import scala.util.parsing.combinator.RegexParsers

trait PartialSbtParser extends RegexParsers {

  def dummyChangeGetter: Parser[ChangeGetter] =
    """dummyChanges""".r ~ """\w+""".r ^^ {
      case changeGetterName ~ logFilePath =>
        changeGetterName match {
          case "dummyChanges" => DummyChangeGetter(logFilePath)
          case _              => ???
        }
    }
  def gitChangeGetter: Parser[ChangeGetter] =
    """gitBranch|gitCommit""".r ~ """\w+""".r ~ """\w+""".r ^^ {
      case changeGetterName ~ firstParm ~ secondParam =>
        changeGetterName match {
          case "gitBranch" => GitBranchChangeGetter(firstParm, secondParam)
          case "gitCommit" => GitCommitChangeGetter(firstParm, secondParam)
        }
    }

  def changeGetter: Parser[ChangeGetter] = dummyChangeGetter | gitChangeGetter
}
