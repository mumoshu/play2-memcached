val appName         = "play2-memcached-" + playShortName
val spymemcached    = "net.spy" % "spymemcached" % "2.12.3"
val h2databaseTest  = "com.h2database" % "h2" % "2.2.224" % Test

lazy val baseSettings = Seq(
  scalaVersion := "2.13.12",
  crossScalaVersions := Seq("2.13.12", "3.3.1"),
  Test / parallelExecution := false
)

def playShortName: String = {
  val version = play.core.PlayVersion.current
  val majorMinor = version.split("""\.""").take(2).mkString("")
  s"play$majorMinor"
}

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
ThisBuild / dynverVTagPrefix := true

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  dynverAssertTagVersion.value
  s
}

lazy val root = Project("root", base = file("."))
  .settings(baseSettings: _*)
  .settings(
    publish / skip := true,
  ).aggregate(plugin, scalaSample, javaSample)

lazy val plugin = Project(appName, base = file("plugin"))
  .settings(baseSettings: _*)
  .settings(
    libraryDependencies += h2databaseTest,
    libraryDependencies += spymemcached,
    libraryDependencies += playCore % Provided,
    libraryDependencies += cacheApi % Provided,
    libraryDependencies += specs2 % Test,
    organization := "com.github.mumoshu",
    //scalacOptions += "-deprecation",
    Test / publishArtifact := false,
    pomIncludeRepository := { _ => false },
    licenses := Seq("Apache License" -> url("https://github.com/mumoshu/play2-memcached/blob/main/LICENSE")),
    developers += Developer(
      "you",
      "KUOKA Yusuke",
      "you",
      url("https://github.com/mumoshu")
    ),
    Compile/ unmanagedSourceDirectories := {
      (Compile / unmanagedSourceDirectories).value ++ Seq((Compile / sourceDirectory).value)
    },
    Test / unmanagedSourceDirectories := {
      (Test / unmanagedSourceDirectories).value ++ Seq((Test / sourceDirectory).value)
    }
  )

lazy val scalaSample = Project(
  "scala-sample",
  file("samples/scala")
).enablePlugins(PlayScala)
.settings(baseSettings: _*)
.settings(
  libraryDependencies += cacheApi,
  libraryDependencies += h2databaseTest,
  publish / skip := true,
).dependsOn(plugin)

lazy val javaSample = Project(
  "java-sample",
  file("samples/java")
).enablePlugins(PlayJava)
.settings(baseSettings: _*)
.settings(
  libraryDependencies += cacheApi,
  libraryDependencies += h2databaseTest,
  publish / skip := true,
).dependsOn(plugin)
