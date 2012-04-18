Memcached Plugin for Play framework 2.0
---------------------------------------

An implementation of CacheAPI for Play 2.0 final.
Using spymemcached 2.6 internally, which is the same as Play 1.2.4.

## Usage

Add the following dependency to your Play project:

```scala
  val appDependencies = Seq(
    "com.github.mumoshu" %% "play2-memcached" % "0.1-SNAPSHOT"
  )
  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    resolvers += "Sonatype OSS Snapshots Repository" at "http://oss.sonatype.org/content/groups/public",
    resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.
  )
```

Add a reference to the plugin in `play.plugins` file.
`play.plugins` file must be put somewhere in the classpath.
My recommendation is to put it in `conf/` directory.

```
  5000:com.github.mumoshu.play2.memcached.MemcachedPlugin
```

Specify the host name or IP address of the memcached server, and the port number in `application.conf`:

```
  memcached.host="127.0.0.1:11211"
```

Then, you can use the `play.api.cache.Cache` object to put a key and a value in memcached:

```scala
 Cache.set("key", "theValue")
```

Or to get a value:

```scala
 val theValue = Cache.getAs[String]("key")
```

## Additional configurations

You can disable the plugin in a similar manner to Play's build-in Ehcache Plugin.
To disable the plugin in `application.conf`:

```
  memcachedplugin=disabled
```

## Build status

[![Build Status](https://secure.travis-ci.org/mumoshu/play2-memcached.png)](http://travis-ci.org/mumoshu/play2-memcached)
