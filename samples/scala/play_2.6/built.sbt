libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.github.mumoshu" %% "play2-memcached-play25" % "0.8.0",
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
