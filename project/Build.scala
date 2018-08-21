import sbt._
import Keys._

import com.typesafe.sbt.pgp.PgpKeys._

object ApplicationBuild extends Build {

  val appName         = "play2-memcached-" + playShortName
  val appVersion      = "0.10.0-M1"

  lazy val baseSettings = Seq(
    parallelExecution in Test := false
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
      publish := {},
      publishLocalSigned := {},
      publishSigned := {}
    ).aggregate(plugin, scalaSample, javaSample)

  lazy val plugin = Project(appName, base = file("plugin"))
    .settings(baseSettings: _*)
    .settings(
      resolvers += "Typesafe Maven Repository" at "https://dl.bintray.com/typesafe/maven-releases/",
      resolvers += "Spy Repository" at "http://files.couchbase.com/maven2",
      resolvers += "Scalaz Bintray Repo"  at "http://dl.bintray.com/scalaz/releases",
      libraryDependencies += "com.h2database" % "h2" % "1.4.196" % Test,
      libraryDependencies += "net.spy" % "spymemcached" % "2.12.3",
      libraryDependencies += "com.typesafe.play" %% "play" % play.core.PlayVersion.current % "provided",
      libraryDependencies += "com.typesafe.play" %% "play-cache" % play.core.PlayVersion.current % "provided",
      libraryDependencies += "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % "provided,test",
      libraryDependencies += "org.specs2" %% "specs2-core" % "3.9.4" % "test",
      organization := "com.github.mumoshu",
      version := appVersion,
      publishTo := {
        val v = version.value
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
      unmanagedSourceDirectories in Compile := {
        (unmanagedSourceDirectories in Compile).value ++ Seq((sourceDirectory in Compile).value)
      },
      unmanagedSourceDirectories in Test := {
        (unmanagedSourceDirectories in Test).value ++ Seq((sourceDirectory in Test).value)
      }
    )

    lazy val scalaSample = Project(
      "scala-sample",
      file("samples/scala")
    ).enablePlugins(play.sbt.PlayScala).settings(
        play.sbt.PlayScala.projectSettings: _*
    ).settings(
      resolvers += "Typesafe Maven Repository" at "https://dl.bintray.com/typesafe/maven-releases/",
      libraryDependencies += play.sbt.PlayImport.cacheApi,
      libraryDependencies += "com.h2database" % "h2" % "1.4.196" % Test,
      parallelExecution in Test := false,
      publishLocal := {},
      publish := {},
      publishLocalSigned := {},
      publishSigned := {}
    ).dependsOn(plugin)

    lazy val javaSample = Project(
      "java-sample",
      file("samples/java")
    ).enablePlugins(play.sbt.PlayJava).settings(baseSettings: _*).settings(
      libraryDependencies += play.sbt.PlayImport.cacheApi,
      libraryDependencies += "com.h2database" % "h2" % "1.4.196" % Test,
      publishLocal := {},
      publish := {},
      publishLocalSigned := {},
      publishSigned := {}
    ).dependsOn(plugin)

}
