libraryDependencies ++= Seq(
  jdbc,
  cacheApi,
  ws,
  "com.github.mumoshu" %% "play2-memcached-play28" % "0.11.0-SNAPSHOT",
  specs2 % Test
)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
