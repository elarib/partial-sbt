name := "dummy-listing-simple-project"

version := "0.1"

scalaVersion := "2.13.1"

enablePlugins(com.elarib.PartialSbtPlugin)

initialize ~= { _ =>
  System.setProperty(
    "log4j.configurationFile",
    "./log4j2.xml")
  System.setProperty(
    "DUMMY_CHANGE_GETTER_PATH",
    "./1stChanges")
}