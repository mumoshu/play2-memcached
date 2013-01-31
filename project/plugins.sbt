ivyLoggingLevel := UpdateLogging.Full

// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
// addSbtPlugin("play" % "sbt-plugin" % "2.0")
//libraryDependencies += Defaults.sbtPluginExtra("play" %% "sbt-plugin" % "2.0", "0.11.2", "2.9.1")
//addSbtPlugin("play" % "sbt-plugin" % "2.1-RC1")

//resolvers += "Spy Repository" at "http://files.couchbase.com/maven2"

// I'm releasing the plugin to a Maven Repository
resolvers += Resolver.url("sbt-plugin-releases", /* no new line */
  new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6")

addSbtPlugin("play" % "sbt-plugin" % "2.1-RC3")
