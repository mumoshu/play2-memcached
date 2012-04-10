publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else                             Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

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
