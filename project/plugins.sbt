ivyLoggingLevel := UpdateLogging.Full

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

val playVersion = scala.util.Properties.envOrElse("PLAY_VERSION", "2.5.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % playVersion)

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")
