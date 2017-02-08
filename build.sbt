name := "fintrospect-example-app"

scalaVersion := "2.12.1"

resolvers += "JCenter" at "https://jcenter.bintray.com"

mainClass in(Test, run) := Some("env.RunnableEnvironment")

scalacOptions := Seq("-deprecation", "-feature")

val fintrospectVersion = "14.12.0"

libraryDependencies ++= Seq(
  "io.fintrospect" %% "fintrospect-core" % fintrospectVersion,
  "io.fintrospect" %% "fintrospect-circe" % fintrospectVersion,
  "io.fintrospect" %% "fintrospect-mustache" % fintrospectVersion,
  "io.circe" %% "circe-optics" % "0.7.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)
