import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play2-memcached"
    val appVersion      = "0.1-SNAPSHOT"


    val appDependencies = Seq(
      "spy" % "spymemcached" % "2.6"
    )

    val main = PlayProject(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
      resolvers += "Spy Repository" at "http://files.couchbase.com/maven2",
      organization := "com.github.mumoshu",
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

    val scalaSample = PlayProject(appName + "-scala-sample", appVersion, path = file("samples/scala"), mainLang = SCALA).settings(
      // Add your own project settings here
    ).dependsOn(main)

    val javaSample = PlayProject(appName + "-java-sample", appVersion, path = file("samples/java"), mainLang = JAVA).settings(
      // Add your own project settings here
    ).dependsOn(main)

}
