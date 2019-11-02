lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "partial-sbt",
    organization := "com.elarib",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.4",
    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    libraryDependencies ++= Seq(
      "org.eclipse.jgit" % "org.eclipse.jgit" % "5.5.1.201910021850-r",
      "org.scalactic" %% "scalactic" % "3.0.8",
      "org.scalatest" %% "scalatest" % "3.0.8" % "test",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5"

    )
  )