libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.github.mumoshu" %% "play2-memcached-play24" % "0.7.0",
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesGenerator := InjectedRoutesGenerator
