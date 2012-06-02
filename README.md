Memcached Plugin for Play framework 2.0
---------------------------------------

An implementation of CacheAPI for Play 2.0 final.
Using spymemcached 2.6 internally, which is the same as Play 1.2.4.

## Usage

Add the following dependency to your Play project:

```scala
  val appDependencies = Seq(
    "com.github.mumoshu" %% "play2-memcached" % "0.2.1-SNAPSHOT"
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

## Additional configurations

### Disabling the plugin

You can disable the plugin in a similar manner to Play's build-in Ehcache Plugin.
To disable the plugin in `application.conf`:

```
  memcachedplugin=disabled
```

### Authentication with SASL

If you memcached requires the client an authentication with SASL, provide username/password like:

```
  memcached.user=misaka
  memcached.password=mikoto
```

### Configure logging

By default, the plugin (or the spymemcached under the hood) does not output any logs at all.
If you need to peek into what's going on, set the log level like:

```
  logger.memcached=DEBUG
```

## Build status

[![Build Status](https://secure.travis-ci.org/mumoshu/play2-memcached.png)](http://travis-ci.org/mumoshu/play2-memcached)
