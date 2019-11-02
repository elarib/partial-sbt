package com.elarib.model

import java.io.File

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.CanonicalTreeParser

import scala.collection.JavaConverters._
import scala.io.Source

sealed trait ChangeGetter {
  def changes: List[File]
}

case class GitBranchChangeGetter(sourceBranch: String, targetBranch: String)
    extends ChangeGetter {

  override def changes: List[File] =
    GitChangeGetterHelper.getBranchDiff(sourceBranch, targetBranch)
}

case class GitCommitChangeGetter(firstCommit: String, secondCommit: String)
    extends ChangeGetter {
  override def changes: List[File] =
    GitChangeGetterHelper.getCommitDiff(firstCommit, secondCommit)
}

object GitChangeGetterHelper {

  private val repository = new FileRepositoryBuilder()
    .setGitDir(new File(s"${System.getProperty("user.dir")}/.git"))
    .readEnvironment
    .findGitDir()
    .setMustExist(true)
    .build()
  private val git = new Git(repository)

  def getBranchDiff(sourceBranch: String, targetBranch: String): List[File] = {

    val oldTree = getBranchRefTree(sourceBranch)
    val newTree = getBranchRefTree(targetBranch)

    gitDiff(oldTree, newTree)
  }

  def getCommitDiff(firstCommit: String, secondCommit: String): List[File] = {

    val oldTree = getCommitRefTree(firstCommit)
    val newTree = getCommitRefTree(secondCommit)

    gitDiff(oldTree, newTree)
  }

  private def gitDiff(oldTree: CanonicalTreeParser,
                      newTree: CanonicalTreeParser) = {
    git
      .diff()
      .setOldTree(oldTree)
      .setNewTree(newTree)
      .call()
      .asScala
      .map { diffEntry =>
        new File(diffEntry.getNewPath)
      }
      .toList
  }

  private def getBranchRefTree(branchName: String) = {
    val head = repository.exactRef(s"refs/heads/$branchName")
    try {
      val walk = new RevWalk(repository)
      try {
        val commit = walk.parseCommit(head.getObjectId)
        val tree = walk.parseTree(commit.getTree.getId)
        val treeParser = new CanonicalTreeParser
        try {
          val reader = repository.newObjectReader
          try treeParser.reset(reader, tree.getId)
          finally if (reader != null) reader.close()
        }
        walk.dispose
        treeParser
      } finally if (walk != null) walk.close()
    }
  }

  private def getCommitRefTree(commitId: String) = {
    try {
      val walk = new RevWalk(repository)
      try {
        val commit = walk.parseCommit(ObjectId.fromString(commitId))
        val tree = walk.parseTree(commit.getTree.getId)
        val treeParser = new CanonicalTreeParser
        try {
          val reader = repository.newObjectReader
          try treeParser.reset(reader, tree.getId)
          finally if (reader != null) reader.close()
        }
        walk.dispose
        treeParser
      } finally if (walk != null) walk.close()
    }
  }

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
