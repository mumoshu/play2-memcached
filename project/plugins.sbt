ivyLoggingLevel := UpdateLogging.Full

val playVersion = scala.util.Properties.envOrElse("PLAY_VERSION", "2.7.0-M2")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % playVersion)

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.2-1")
