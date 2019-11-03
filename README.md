[![Build Status](https://travis-ci.com/elarib/partial-sbt.svg?branch=master)](https://travis-ci.com/elarib/partial-sbt)

Partial-sbt
============

The goal is simple: Apply some sbt tasks/commands on only the modules/sub-modules changed between two git commits or two git branches, including their reverse dependencies.

For example: 
 - Test and deploy only modules (and their reverse dependencies) that have been changed between develop branch and feature x branch
 - Package only modules (and their reverse dependencies) that have been changed between two git commits.

Requirements
------------

* SBT
* Git managed repository.

Setup
-----

### Using Published Plugin

⚠️ Not published yet

Add sbt-assembly as a dependency in `project/plugins.sbt`:

```scala
addSbtPlugin("com.elarib" % "partial-sbt" % "x.y.z")
```

Usage
-----

### Applying the plugin in your build.sbt

```scala
enablePlugins(com.elarib.PartialSbtPlugin)
```

### `changedProjects` task

Now you have the `changedProjects` task, that list you all the modules (and their reverse dependencies) changed.
You have the possibility to get the changed projects  :
- Between two git branches using: 
    ```sbt
    > changedProjects gitBranch sourceLocalBranch targetLocalBranch
    ```
- Between two git commits using: 
    ```scala
    > changedProjects gitCommit oldCommitId newCommitId
    ```


### `metaBuildChangedFiles` task

List you all the changed meta build files  :
- Between two git branches using: 
    ```sbt
    > metaBuildChangedFiles gitBranch sourceLocalBranch targetLocalBranch
    ```
- Between two git commits using: 
    ```scala
    > metaBuildChangedFiles gitCommit oldCommitId newCommitId
    ```

How it works
------------

### 1. Get changed files

`jgit` is used to list the diff between two branches or two commits.

### 2. Check if there is some main metabuild file changed

If yes, the whole project was changed, and they will be a need to reload (or build/test ...)
If no, continue to next step.

### 3. List all modules impacted

List all modules impacted based on the changed files (step1) with all their reverse dependencies.

And finally list them, or apply the command or task need (not implemented yet).


TODO
------------
- [ ] Apply commands to changed modules.
- [ ] Deploy to some maven repo manager.

License
-------

Copyright (c) 2019 [Abdelhamide EL ARIB](https://twitter.com/elarib29) 

Published under The MIT License.