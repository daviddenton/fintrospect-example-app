name := "fintrospect-example-app"

scalaVersion := "2.11.8"

mainClass in (Test, run) := Some("env.RunnableEnvironment")

resolvers += "JCenter" at "https://jcenter.bintray.com"

libraryDependencies ++= Seq(
  "io.github.daviddenton" %% "fintrospect" % "12.17.0",
  "com.twitter" %% "finagle-http" % "6.35.0",
  "com.github.spullara.mustache.java" % "compiler" % "0.9.1",
  "com.github.spullara.mustache.java" % "scala-extensions-2.11" % "0.9.1",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)
