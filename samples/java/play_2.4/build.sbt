libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "com.github.mumoshu" %% "play2-memcached" % "0.7.0"
)

routesGenerator := InjectedRoutesGenerator
