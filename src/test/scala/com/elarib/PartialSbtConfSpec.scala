package com.elarib

import com.elarib.model._
import org.scalatest.{FlatSpec, Matchers}

class PartialSbtConfSpec extends FlatSpec with Matchers with PartialSbtParser {

  "PartialSbtParser" should "parse corretly DummyChangeGetter" in {
    parse(changeGetter, "dummyChanges") should matchPattern {
      case Success(DummyChangeGetter, _) =>
    }

    parse(changeGetter, "gitBranch firstBranch secondBranch") should matchPattern {
      case Success(GitBranchChangeGetter("firstBranch", "secondBranch"), _) =>
    }

    parse(changeGetter, "gitCommit firstCommit secondCommit") should matchPattern {
      case Success(GitCommitChangeGetter("firstCommit", "secondCommit"), _) =>
    }

  }
}
