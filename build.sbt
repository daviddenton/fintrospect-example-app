name := "fintrospect-example-app"

scalaVersion := "2.11.8"

resolvers += "JCenter" at "https://jcenter.bintray.com"

mainClass in(Test, run) := Some("env.RunnableEnvironment")

libraryDependencies ++= Seq(
  "io.fintrospect" %% "fintrospect-core" % "13.9.0",
  "io.fintrospect" %% "fintrospect-json4s" % "13.9.0",
  "io.fintrospect" %% "fintrospect-mustache" % "13.9.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)
