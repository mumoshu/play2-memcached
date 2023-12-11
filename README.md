Memcached Plugin for Play framework 2.x
---------------------------------------

A Memcached implementation of Cache API for Play 2.x.
Using spymemcached internally, which is the same as Play 1.x's default Cache implementation.

## Usage

Add the following dependency to your Play project:

### Library dependencies

For Play 2.6.x and newer:
!!! Changed `play.modules.cache.*` config keys to `play.cache.*` !!!

```scala
  val appDependencies = Seq(
    play.PlayImport.cacheApi,
    "com.github.mumoshu" %% "play2-memcached-play26" % "0.9.2"
  )
  val main = Project(appName).enablePlugins(play.PlayScala).settings(
    version := appVersion,
    libraryDependencies ++= appDependencies,
    resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.
  )
```

For Play 2.5.x:

```scala
  val appDependencies = Seq(
    play.PlayImport.cache,
    "com.github.mumoshu" %% "play2-memcached-play25" % "0.8.0"
  )
  val main = Project(appName).enablePlugins(play.PlayScala).settings(
    version := appVersion,
    libraryDependencies ++= appDependencies,
    resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.
  )
```

For Play 2.4.x:

```scala
  val appDependencies = Seq(
    play.PlayImport.cache,
    "com.github.mumoshu" %% "play2-memcached-play24" % "0.7.0"
  )
  val main = Project(appName).enablePlugins(play.PlayScala).settings(
    version := appVersion,
    libraryDependencies ++= appDependencies,
    resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.
  )
```

For Play 2.3.x:

```scala
  val appDependencies = Seq(
    play.PlayImport.cache,
    "com.github.mumoshu" %% "play2-memcached-play23" % "0.7.0"
  )
  val main = Project(appName).enablePlugins(play.PlayScala).settings(
    version := appVersion,
    libraryDependencies ++= appDependencies,
    resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.
  )
```

For Play 2.2.0:

```scala
  val appDependencies = Seq(
    cache, // or play.Project.cache if not imported play.Project._
    "com.github.mumoshu" %% "play2-memcached" % "0.4.0" // or 0.5.0-RC1 to try the latest improvements
  )
  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.
  )
```


For Play 2.1.0:

```scala
  val appDependencies = Seq(
    "com.github.mumoshu" %% "play2-memcached" % "0.3.0.3"
  )
  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.
  )
```

For Play 2.0:

```scala
  val appDependencies = Seq(
    "com.github.mumoshu" %% "play2-memcached" % "0.2.4.3"
  )
  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.
  )
```

### Configurations

#### Starting with Play 2.6.x

```
play.modules.enabled+="com.github.mumoshu.play2.memcached.MemcachedModule"

# Well-known configuration provided by Play
play.cache.defaultCache=default
play.cache.bindCaches=["db-cache", "user-cache", "session-cache"]

# Tell play2-memcached where your memcached host is located at
memcached.host="127.0.0.1:11211"
```

#### For Play 2.4.x and above

```
play.modules.enabled+="com.github.mumoshu.play2.memcached.MemcachedModule"

# To avoid conflict with play2-memcached's Memcached-based cache module
play.modules.disabled+="play.api.cache.EhCacheModule"

# Well-known configuration provided by Play
play.modules.cache.defaultCache=default
play.modules.cache.bindCaches=["db-cache", "user-cache", "session-cache"]

# Tell play2-memcached where your memcached host is located at
memcached.host="127.0.0.1:11211"
```

#### Play 2.3.x or below

Add a reference to the plugin in `play.plugins` file.
`play.plugins` file must be put somewhere in the classpath.
My recommendation is to put it in `conf/` directory.

```
  5000:com.github.mumoshu.play2.memcached.MemcachedPlugin
```

First of all, in `application.conf`, disable the EhCachePlugin - Play's default implementation of CacheAPI:

```
  ehcacheplugin=disabled
```

Specify the host name or IP address of the memcached server, and the port number:

```
  memcached.host="127.0.0.1:11211"
```

If you have multiple memcached instances over different host names or IP addresses, provide them like:

```
  memcached.1.host="mumocached1:11211"
  memcached.2.host="mumocached2:11211"
```

### Code examples

#### For Play 2.4.x and above

See the Play Framework documentation for the [Scala](https://www.playframework.com/documentation/2.6.x/ScalaCache) and [Java](https://www.playframework.com/documentation/2.6.x/JavaCache) API.

#### For Play 2.3.x or below

Then, you can use the `play.api.cache.Cache` object to store a value in memcached:

```scala
 Cache.set("key", "theValue")
```

This way, memcached tries to retain the stored value eternally.
Of course Memcached does not guarantee eternity of the value, nor can it retain the value on restart.

If you want the value expired after some time:

```scala
 Cache.set("key", "theValueWithExpirationTime", 3600)
 // The value expires after 3600 seconds.
```

To get the value for a key:

```scala
 val theValue = Cache.getAs[String]("key")
```

You can remove the value (It's not yet a part of Play 2.0's Cache API, though):

```scala
 play.api.Play.current.plugin[MemcachedPlugin].get.api.remove("keyToRemove")
```

### Advanced configurations

#### Disabling the plugin (For Play 2.3.x or below)

You can disable the plugin in a similar manner to Play's build-in Ehcache Plugin.
To disable the plugin in `application.conf`:

```
  memcachedplugin=disabled
```

#### Authentication with SASL

If you memcached requires the client an authentication with SASL, provide username/password like:

```
  memcached.user=misaka
  memcached.password=mikoto
```

#### Configure logging

By default, the plugin (or the spymemcached under the hood) does not output any logs at all.
If you need to peek into what's going on, set the log level like:

##### For Play 2.4.x and above

In your `logback.xml`:

```
  <logger name="memcached" level="DEBUG" />
```

#### For Play 2.3.x or below

```
  logger.memcached=DEBUG
```

#### Namespacing

You can prefix every key to put/get/remove with a global namespace.

##### For Play 2.4.x and above

You can inject an `(ASync)CacheApi` with @play.cache.NamedCache to prefix all the keys you get, set and remove with the given namespace.
There is more documentation in the official Play Framework documentation.

```
  @Inject @play.cache.NamedCache("user-cache") private AsyncCacheApi cacheApi;
```

##### For Play 2.3.x or below

By default, the namespace is an empty string, implying you don't use namespacing at all.
To enable namespacing, configure it in "application.conf":

```
  memcached.namespace=mikoto.
```

### Key hashing

**Requires play2-memcached 0.9.0 or later**

You may consider activating this option to enable support for long keys which are composed of more than 250 characters.

It is disabled by default like:

```
  memcached.hashkeys=off
```

You can activate it by specifying the name of a message digest algorithm used for hashing:

```
  memcached.hashkeys=SHA-1
```

Available options are MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512.
Please refer to http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#MessageDigest for the complete list of available algorithms.

### Configuring timeouts

Until Play version 2.6 you can specify timeouts for obtaining values from Memcached.
This option isn't needed anymore since Play 2.6 because since that version Play's cache api is async per default.

```
  # Timeout in 1 second (only until Play version 2.6)
  memcached.timeout=1
```

### Using ElastiCache's Auto-Discovery feature

At first, download the latest `AmazonElastiCacheClusterClient-*.jar` from the AWS console,
or build it yourself as described in [The Amazon ElastiCache Cluster Client page in GitHub](https://github.com/amazonwebservices/aws-elasticache-cluster-client-memcached-for-java),
and put it under `/lib`.

Remove the SBT dependency on spymemcached by excluding it from play2-mamcached's transitive dependencies:

```
  "com.github.mumoshu" %% "play2-memcached" % "0.5.0-RC1 exclude("net.spy", "spymemcached")
```

Configure your configuration endpoint in `application.conf`:

```
  elasticache.config.endpoint="mycachename.asdfjk.cfg.use1.cache.amazonaws.com:11211".
```

### Version history

0.2.2 Fixed the logging leak issue. You don't get a bunch of INFO messages to play app's default logger anymore.

0.2.3 Allow removing keys in both Java and Scala ways described in Play's documentation. See MemcachedIntegrationSpec.scala for how to remove keys in Scala.

0.2.4 Introduced "namespace" to prefix every key to put/get/remove with a global namespace configured in "application.conf"

0.2.4.1 Updated spymemcached to 2.8.12

0.2.4.3 Updated spymemcached to 2.9.0 which solves the authentication issues.

0.3.0 Built for Play 2.1.0 and available in the Maven Central. Also updated spymemcached to 2.8.4.

0.3.0.1 Updated spymemcached to 2.8.12

0.3.0.2 Reverted spymemcached to 2.8.9 to deal with authentication failures to various memcache servers caused by spymemcached 2.8.10+. See #17 and #20 for details.

0.3.0.3 Updated spymemcached to 2.9.0 which solves the authentication issues.

0.4.0 Build for Play 2.2.0

0.5.0-RC1 Improvements:
  #14 Adding support for Amazon Elasticache (thanks to @kamatsuoka)
  #23 Adding configurable timeouts on the future (thanks to @rmmeans)
  #24 Empty keys - kind of ehcache compilance, avoiding IllegalArgumentExceptions (thanks to @mkubala)

0.7.0 Cross built for Play 2.3.x, 2.4.x, Scala 2.10.5 and 2.11.6. Artifact IDs are renamed to `play2-memcached-play2{3,4}_2.1{0,1}`

0.8.0 Built for Play 2.5.x and Scala 2.11.11. Artifact ID for this build is `play2-memcached-play25_2.11`

0.9.0 Built for Play 2.6.x and Scala 2.11.11 and 2.12.3. Artifact ID for this build is `play2-memcached-play26_2.1{1,2}`
  !!! Changed `play.modules.cache.*` config keys to `play.cache.*` !!!

0.9.1 Remove global state by removing reference to deprecated Play.current

0.9.2 Fix frozen future in 2.6 API

0.10.0-M1 Built for Play 2.7.0-M1 and Scala 2.11.12 and 2.12.6. Artifact ID for this build is `play2-memcached-play27_2.1{1,2}`

0.10.0-M2 Built for Play 2.7.0-M2 and Scala 2.11.12 and 2.12.6. Artifact ID for this build is `play2-memcached-play27_2.1{1,2}`

0.10.0-RC3 Built for Play 2.7.0-RC8 and Scala 2.11.12 and 2.12.7. Artifact ID for this build is `play2-memcached-play27_2.1{1,2}`

0.10.0 Built for Play 2.7.0 and Scala 2.11.12 and 2.12.8. Artifact ID for this build is `play2-memcached-play27_2.1{1,2}`

0.10.1 Built for Play 2.7.3 and Scala 2.13.0, 2.11.12 and 2.12.8. Artifact ID for this build is `play2-memcached-play27_2.1{1,2,3}`

0.11.0 Built for Play 2.8.0 and Scala 2.13.1 and 2.12.10. Artifact ID for this build is `play2-memcached-play28_2.1{2,3}`

0.12.0 Built for Play 2.9.0 and Scala 2.13.12 and 3.3.1. Artifact ID for this build is `play2-memcached-play29_[2.13|3]`

### Releasing play2-memcached / Publishing to the maven central

1. Tag the release: `git tag -s 1.2.3`
1. Push tag: `git push upstream 1.2.3`
1. GitHub action workflow does the rest: https://github.com/mumoshu/play2-memcached/actions/workflows/publish.yml
  - Snapshots are published to https://oss.sonatype.org/content/repositories/snapshots/com/github/mumoshu/
  - Releases are published to https://repo1.maven.org/maven2/com/github/mumoshu/ (takes up to 1 hour)


### Acknowledgement

Thanks to:
@gakuzzzz for the original idea of "namespacing" and the initial pull request for it.
@kamatsuoka for adding support for Amazon Elasticache.
@rmmeans for adding configurable timeouts on the future.
@mkubala for improving compliance with EhCache.

### Build status

[![Build Status](https://secure.travis-ci.org/mumoshu/play2-memcached.png)](http://travis-ci.org/mumoshu/play2-memcached)
