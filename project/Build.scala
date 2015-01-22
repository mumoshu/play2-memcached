import sbt._
import Keys._

object ApplicationBuild extends Build {

  val appName         = "play2-memcached"
  val appVersion      = "0.7.0"
  val appScalaVersion = "2.10.4"
  val appScalaCrossVersions = appScalaVersion :: "2.11.1" :: Nil

  lazy val baseSettings = Seq(
    scalaVersion := appScalaVersion,
    crossScalaVersions := appScalaCrossVersions,
    parallelExecution in Test := false
  )

  def playVersionSpecificUnmanagedSourceDirectories(sds: Seq[java.io.File], sd: java.io.File) = {
    val v = play.core.PlayVersion.current
    val mainVersion = v.split("""\.""").take(2).mkString(".")
    val versionSpecificSrc = new java.io.File(sd, "play_" + mainVersion)
    val defaultSrc = new java.io.File(sd, "default")
    (if (versionSpecificSrc.exists) Seq(versionSpecificSrc) else Seq(defaultSrc)) ++ sds
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
      libraryDependencies += "org.specs2" % "specs2_2.10" % "2.4.15" % "test",
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

    lazy val scalaSample = Project(
      "scala-sample",
      file("samples/scala")
    ).enablePlugins(play.PlayScala).settings(
      resolvers += "Typesafe Maven Repository" at "http://repo.typesafe.com/typesafe/maven-releases/",
      libraryDependencies += play.PlayImport.cache,
      scalaVersion := appScalaVersion,
      crossScalaVersions := appScalaCrossVersions,
      parallelExecution in Test := false,
      publishLocal := {},
      publish := {}
    ).dependsOn(plugin)

    lazy val javaSample = Project(
      "java-sample",
      file("samples/java")
    ).enablePlugins(play.PlayJava).settings(baseSettings: _*).settings(
      libraryDependencies += play.PlayImport.cache,
      publishLocal := {},
      publish := {}
    ).dependsOn(plugin)

}
