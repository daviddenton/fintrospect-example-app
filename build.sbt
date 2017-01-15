name := "fintrospect-example-app"

scalaVersion := "2.11.8"

resolvers += "JCenter" at "https://jcenter.bintray.com"

mainClass in(Test, run) := Some("env.RunnableEnvironment")

scalacOptions := Seq("-deprecation", "-feature")

val fintrospectVersion = "14.3.0"

libraryDependencies ++= Seq(
  "io.fintrospect" %% "fintrospect-core" % fintrospectVersion,
  "io.fintrospect" %% "fintrospect-circe" % fintrospectVersion,
  "io.fintrospect" %% "fintrospect-mustache" % fintrospectVersion,
  "io.circe" %% "circe-optics" % "0.6.1",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)
