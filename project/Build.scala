import sbt._
import Keys._

object ApplicationBuild extends Build {

  val appName         = "play2-memcached-" + playShortName
  val appVersion      = "0.7.0"

  lazy val baseSettings = Seq(
    parallelExecution in Test := false
  )

  def playShortName: String = {
    val version = play.core.PlayVersion.current
    val majorMinor = version.split("""\.""").take(2).mkString("")
    s"play$majorMinor"
  }

  def playMajorAndMinorVersion: String = {
    val v = play.core.PlayVersion.current
    v.split("""\.""").take(2).mkString(".")
  }

  def isPlay24: Boolean = {
    playMajorAndMinorVersion == "2.4"
  }

  def playVersionSpecificSourceDirectoryUnder(sd: java.io.File): java.io.File = {
    val versionSpecificSrc = new java.io.File(sd, "play_" + playMajorAndMinorVersion)
    val defaultSrc = new java.io.File(sd, "default")
    if (versionSpecificSrc.exists) versionSpecificSrc else defaultSrc
  }

  def playVersionSpecificUnmanagedSourceDirectories(sds: Seq[java.io.File], sd: java.io.File) = {
    Seq(playVersionSpecificSourceDirectoryUnder(sd)) ++ sds
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
      resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      resolvers += "Typesafe Maven Repository" at "http://repo.typesafe.com/typesafe/maven-releases/",
      resolvers += "Spy Repository" at "http://files.couchbase.com/maven2",
      resolvers += "Scalaz Bintray Repo"  at "http://dl.bintray.com/scalaz/releases",
      libraryDependencies += "net.spy" % "spymemcached" % "2.9.0",
      libraryDependencies += "com.typesafe.play" %% "play" % play.core.PlayVersion.current % "provided",
      libraryDependencies += "com.typesafe.play" %% "play-cache" % play.core.PlayVersion.current % "provided",
      libraryDependencies += "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % "provided,test",
      libraryDependencies += "org.specs2" %% "specs2" % "2.4.15" % "test",
      organization := "com.github.mumoshu",
      version := appVersion,
      publishTo <<= version { v: String =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
        else                             Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      publishMavenStyle := true,
      publishArtifact in Test := false,
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
      unmanagedSourceDirectories in Compile <<= (unmanagedSourceDirectories in Compile, sourceDirectory in Compile) { (sds: Seq[java.io.File], sd: java.io.File) =>
        playVersionSpecificUnmanagedSourceDirectories(sds, sd)
      },
      unmanagedSourceDirectories in Test <<= (unmanagedSourceDirectories in Test, sourceDirectory in Test) { (sds: Seq[java.io.File], sd: java.io.File) =>
        playVersionSpecificUnmanagedSourceDirectories(sds, sd)
      }
    )

    def playCache: ModuleID = {
      play.PlayImport.cache
    }

    def playScalaPlugin: AutoPlugin = {
      play.PlayScala
    }

    lazy val scalaSample = Project(
      "scala-sample",
      playVersionSpecificSourceDirectoryUnder(file("samples/scala"))
    ).enablePlugins(playScalaPlugin).settings(
      resolvers += "Typesafe Maven Repository" at "http://repo.typesafe.com/typesafe/maven-releases/",
      libraryDependencies += playCache,
      parallelExecution in Test := false,
      publishLocal := {},
      publish := {}
    ).dependsOn(plugin)

    lazy val javaSample = Project(
      "java-sample",
      playVersionSpecificSourceDirectoryUnder(file("samples/java"))
    ).enablePlugins(play.PlayJava).settings(baseSettings: _*).settings(
      libraryDependencies += play.PlayImport.cache,
      publishLocal := {},
      publish := {}
    ).dependsOn(plugin)

}
