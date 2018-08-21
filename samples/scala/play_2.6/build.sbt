libraryDependencies ++= Seq(
  jdbc,
  cacheApi,
  ws,
  "com.github.mumoshu" %% "play2-memcached-play26" % "0.9.2",
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
