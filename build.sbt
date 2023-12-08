val appName         = "play2-memcached-" + playShortName
val appVersion      = "0.12.0-SNAPSHOT"
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

lazy val root = Project("root", base = file("."))
  .settings(baseSettings: _*)
  .settings(
    publishLocal := {},
    publish := {}
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
    version := appVersion,
    //scalacOptions += "-deprecation",
    publishTo := {
      val v = version.value
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
      else                             Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true,
    Test / publishArtifact := false,
    pomIncludeRepository := { _ => false },
    pomExtra := (
      <url>https://github.com/mumoshu/play2-memcached</url>
        <licenses>
          <license>
            <name>BSD-style</name>
            <url>http://www.opensource.org/licenses/bsd-license.php</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:mumoshu/play2-memcached.git</url>
          <connection>scm:git:git@github.com:mumoshu/play2-memcached.git</connection>
        </scm>
        <developers>
          <developer>
            <id>you</id>
            <name>KUOKA Yusuke</name>
            <url>https://github.com/mumoshu</url>
          </developer>
        </developers>
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
  publishLocal := {},
  publish := {}
).dependsOn(plugin)

lazy val javaSample = Project(
  "java-sample",
  file("samples/java")
).enablePlugins(PlayJava)
.settings(baseSettings: _*)
.settings(
  libraryDependencies += cacheApi,
  libraryDependencies += h2databaseTest,
  publishLocal := {},
  publish := {}
).dependsOn(plugin)
