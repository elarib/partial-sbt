def settings(projectId: String) = Seq(
  name := projectId,
  organization := "com.elarib",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.4",
  //    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  //      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
  //    },
  scriptedBufferLog := false
)
enablePlugins(com.elarib.PartialSbtPlugin)

commands +=  Command("addEnvVar")(
  _ => sbt.internal.util.complete.Parsers.spaceDelimited("<arg>")
)((st, args) => {
  System.setProperty(args(0), args(1))
  st
})

// Projects
//                               +-----------+                        +-----------+
//                               |           |                        |           |
//                               |   lib-1   +<---------+  +--------->+   lib-2   |
//                               |           |          |  |          |           |
//                               +-----^-----+          |  |          +---+-------+
//                                     |                |  |              ^
//                                     |                |  |              |
//                                     |                |  |              |
//                                     |                |  |              |
//                      +----------+   |            +---+--+---+          |  +----------+
//                      |          |   |            |          |          |  |          |
//          +----------->  tool-1  +---+            |  tool-3  |          +--+  tool-2  +<----------+
//          |           |          |                |          |             |          |           |
//          |           +----+-----+                +----+-----+             +-----+----+           |
//          |                ^                           ^                         ^                |
//          |                |                           |                         |                |
//          |                |                           |                         |                |
//          |                |                           |                         |                |
//          |                |                           |                         |                |
//          |                |                           |                         |                |
//          |                |                           |                         |                |
//          |         +------+------+             +------+------+           +------+------+         |
//          |         |             |             |             |           |             |         |
//          |         |  service-1  |             |  service-3  |           |  service-2  |         |
//          |         |             |             |             |           |             |         |
//          |         +-------------+             +-------------+           +-------------+         |
//          |                                                                                       |
//          |                                                                                       |
//          |                                                                                       |
//          |                                                                                       |
//          |                                                                                       |
//          |                                      +-------------+                                  |
//          |                                      |             |                                  |
//          +--------------------------------------+  service-4  +----------------------------------+
//                                                 |             |
//                                                 +-------------+


val src = file("src")
val libs = src / "libs"
val tools = src / "tools"
val service = src / "services"

//Libs
lazy val firstLib = sbt.Project("lib-1", libs / "lib-1")
  .settings(settings("lib-1"))

lazy val secondLib = sbt.Project("lib-2", libs / "lib-2")
  .settings(settings("lib-2"))


//Tools
lazy val firstTool = sbt.Project("tool-1", tools / "tool-1")
  .dependsOn(firstLib)
  .settings(settings("tool-1"))

lazy val secondTool = sbt.Project("tool-2", tools / "tool-2")
  .dependsOn(secondLib)
  .settings(settings("tool-2"))

lazy val thirdTool = sbt.Project("tool-3", service / "tool-3")
  .dependsOn(firstLib, secondLib)
  .settings(settings("tool-3"))

//services
lazy val firstService = sbt.Project("service-1", service / "service-1")
  .dependsOn(firstTool)
  .settings(settings("service-1"))

lazy val secondService = sbt.Project("service-2", service / "service-2")
  .dependsOn(secondTool)
  .settings(settings("service-2"))

lazy val thirdService = sbt.Project("service-3", service / "service-3")
  .dependsOn(thirdTool)
  .settings(settings("service-3"))

lazy val fourthService = sbt.Project("service-4", service / "service-4")
  .dependsOn(firstTool, secondTool)
  .settings(settings("service-4"))

