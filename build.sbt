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
    scriptedBufferLog := false
  )

