name := "fintrospect-example-app"

scalaVersion := "2.11.8"

resolvers += "JCenter" at "https://jcenter.bintray.com"

mainClass in(Test, run) := Some("env.RunnableEnvironment")

libraryDependencies ++= Seq(
  "io.fintrospect" %% "fintrospect-core" % "13.18.0",
  "io.fintrospect" %% "fintrospect-circe" % "13.18.0",
  "io.fintrospect" %% "fintrospect-mustache" % "13.18.0",
  "io.circe" %% "circe-optics" % "0.6.1",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)
