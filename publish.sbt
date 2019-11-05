ThisBuild / organization := "com.elarib"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/elarib/partial-sbt"),
    "scm:git@github.com:elarib/partial-sbt.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "elarib",
    name  = "Abdelhamide EL ARIB",
    email = "elarib.abdelhamide@gmail.com",
    url   = url("http://elarib.com")
  )
)

ThisBuild / description := " Apply some sbt task/commands on only the modules/sub-modules (and their reverse dependencies) based on git changes"
ThisBuild / licenses := List("MIT" -> new URL("https://github.com/elarib/partial-sbt/blob/master/LICENSE"))
ThisBuild / homepage := Some(url("https://github.com/elarib/partial-sbt"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true