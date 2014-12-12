ivyLoggingLevel := UpdateLogging.Full

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

val playVersion = scala.util.Properties.envOrElse("PLAY_VERSION", "2.4.0-M2")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % playVersion)

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.1")
