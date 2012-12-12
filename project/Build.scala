import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play2-memcached"
    val appVersion      = "0.2.5-SNAPSHOT"

  lazy val root = Project("root", base = file("."))
    .dependsOn(plugin)
    .aggregate(scalaSample, javaSample)

  lazy val plugin = Project(appName, base = file("plugin")).settings(
      scalaVersion := "2.10.0-RC1",
      // RCs are not binary compatible with the final version
//      scalaBinaryVersion := "2.10.0-RC5",
      crossScalaVersions := Seq("2.10.0-RC1"),
      resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      resolvers += "Spy Repository" at "http://files.couchbase.com/maven2",
      libraryDependencies += "spy" % "spymemcached" % "2.6",
      libraryDependencies += "play" %% "play" % "2.1-RC1" cross CrossVersion.binary,
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
        )
    )

    lazy val scalaSample = play.Project("scala-sample", path = file("samples/scala")).settings(
      scalaVersion := "2.10.0-RC1",
      crossScalaVersions := Seq("2.10.0-RC1"),
      parallelExecution in Test := false
    ).dependsOn(plugin)

    lazy val javaSample = play.Project("java-sample", path = file("samples/java")).settings(
      scalaVersion := "2.10.0-RC1",
      crossScalaVersions := Seq("2.10.0-RC1"),
      parallelExecution in Test := false
    ).dependsOn(plugin)

}
