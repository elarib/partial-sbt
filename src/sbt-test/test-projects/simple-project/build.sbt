name := "simple-project"

version := "0.1"

scalaVersion := "2.13.1"

enablePlugins(com.elarib.PartialSbtPlugin)

commands +=  Command("addEnvVar")(
  _ => sbt.internal.util.complete.Parsers.spaceDelimited("<arg>")
)((st, args) => {
  System.setProperty(args(0), args(1))
  st
})