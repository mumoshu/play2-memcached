libraryDependencies ++= Seq(
  jdbc,
  cacheApi,
  ws,
  "com.github.mumoshu" %% "play2-memcached-play27" % "0.10.0-RC3",
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
