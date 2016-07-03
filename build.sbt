name := "fintrospect-example-app"

scalaVersion := "2.11.8"

mainClass in(Test, run) := Some("env.RunnableEnvironment")

libraryDependencies ++= Seq(
  "io.fintrospect" %% "fintrospect-core" % "13.2.1",
  "io.fintrospect" %% "fintrospect-json4s" % "13.2.1",
  "io.fintrospect" %% "fintrospect-mustache" % "13.2.1",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)
