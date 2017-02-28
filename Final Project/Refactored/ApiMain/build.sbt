
name := "api-main"

version := "1.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.17"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.13",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.12",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,

  "com.typesafe.akka" %% "akka-http-core" % "10.0.3",
  "com.typesafe.akka" %% "akka-http" % "10.0.3",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.3",

  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.3",
  "io.spray"          %%  "spray-json"    % "1.2.5",

  "com.typesafe.akka" %% "akka-http-jackson" % "10.0.3",
  "com.typesafe.akka" %% "akka-http-xml" % "10.0.3",

  "net.liftweb" %% "lift-json" % "2.6-M4"
)

lazy val commonSettings = Seq(
  organization := "MicroServices.ApiMai",
  version := "1.0",
  scalaVersion := "2.11.8"
)

lazy val root = (project in file(".")).
  aggregate(src)

/*lazy val FP = (project in file("KafkaConsumerForTopicOne")).
  settings(commonSettings: _*)*/

//  packageArchetype.java_server
lazy val src =  {
  import com.typesafe.sbt.packager.docker._

  Project(
    id = "src",
    base = file("."),
    settings = commonSettings ++ Seq(
      mainClass in Compile := Some("ApiMain"),
      dockerCommands := dockerCommands.value.filterNot {
        // ExecCmd is a case class, and args is a varargs variable, so you need to bind it with @
        case Cmd("USER", args@_*) => true
        // dont filter the rest
        case cmd => false
      },
      version in Docker := "latest",
      dockerExposedPorts := Seq(8080),
      maintainer in Docker := "aniketkdm@gmail.com",
      dockerBaseImage := "java:8"
    )
  )//.dependsOn(FP)
    .enablePlugins(JavaAppPackaging)
}
