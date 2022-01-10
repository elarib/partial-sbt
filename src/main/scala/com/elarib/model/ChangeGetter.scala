package com.elarib.model

import java.io.File
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.CanonicalTreeParser

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.{Failure, Success, Try}

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
        val path = if (diffEntry.getNewPath == DiffEntry.DEV_NULL)
          diffEntry.getOldPath else diffEntry.getNewPath
        new File(path)
      }
      .toList
  }

  private def getBranchRefTree(branchName: String) = {
    Try {
      val head = repository.exactRef(s"refs/heads/$branchName")
      val walk = new RevWalk(repository)
      val commit = walk.parseCommit(head.getObjectId)
      val tree = walk.parseTree(commit.getTree.getId)
      val treeParser = new CanonicalTreeParser
      val reader = repository.newObjectReader
      Try {
        treeParser.reset(reader, tree.getId)
      }.map(_ => reader.close())
      walk.dispose
      (walk, treeParser)
    } match {
      case Failure(exception) => throw exception
      case Success((walk, treeParser)) =>
        walk.close()
        treeParser
    }
  }

  private def getCommitRefTree(commitId: String) = {
    Try {
      val walk = new RevWalk(repository)
      val commit = walk.parseCommit(ObjectId.fromString(commitId))
      val tree = walk.parseTree(commit.getTree.getId)
      val treeParser = new CanonicalTreeParser
      val reader = repository.newObjectReader
      Try {
        treeParser.reset(reader, tree.getId)
      }.map(_ => reader.close())
      walk.dispose
      (walk, treeParser)
    } match {
      case Failure(exception) => throw exception
      case Success((walk, treeParser)) =>
        walk.close()
        treeParser
    }
  }

}

case class DummyChangeGetter(logFilePath: String) extends ChangeGetter {

  override def changes: List[File] =
    Source
      .fromFile(new File(logFilePath))
      .getLines()
      .map {
        case line if line.nonEmpty => new File(line)
      }
      .toList
}
