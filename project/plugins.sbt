ivyLoggingLevel := UpdateLogging.Full

val playVersion = scala.util.Properties.envOrElse("PLAY_VERSION", "2.9.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % playVersion)

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.10.0")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")
