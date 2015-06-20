libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "com.github.mumoshu" %% "play2-memcached-play24" % "0.7.0"
)

routesGenerator := InjectedRoutesGenerator
