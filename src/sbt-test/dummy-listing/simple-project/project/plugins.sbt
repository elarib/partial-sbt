lazy val root = (project in file(".")).dependsOn(partialSbtPlugin)

lazy val partialSbtPlugin = RootProject(uri("file:///Users/elarib/IdeaProjects/partial-sbt"))