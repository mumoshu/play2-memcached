import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play2-memcached"
    val appVersion      = "1.0-SNAPSHOT"


    val appDependencies = Seq(
      "spy" % "spymemcached" % "2.6"
    )

    val main = PlayProject(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
      resolvers += "Spy Repository" at "http://files.couchbase.com/maven2"
    )

    val scalaSample = PlayProject(appName + "-scala-sample", appVersion, path = file("samples/scala"), mainLang = SCALA).settings(
      // Add your own project settings here
    ).dependsOn(main)

}
