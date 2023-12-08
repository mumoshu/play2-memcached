package com.github.mumoshu.play2.memcached

import akka.stream.Materializer

import play.api.cache._
import play.api.inject._

import play.cache.{ AsyncCacheApi => JavaAsyncCacheApi, SyncCacheApi => JavaSyncCacheApi, DefaultAsyncCacheApi => JavaDefaultAsyncCacheApi, DefaultSyncCacheApi => JavaDefaultSyncCacheApi, NamedCacheImpl }

import javax.inject.{Inject, Singleton, Provider}

import net.spy.memcached.MemcachedClient

import scala.reflect.ClassTag

class MemcachedModule extends SimpleModule((environment, configuration) => {

  import scala.jdk.CollectionConverters._

  val defaultCacheName = configuration.underlying.getString("play.cache.defaultCache")
  val bindCaches = configuration.underlying.getStringList("play.cache.bindCaches").asScala

  // Creates a named cache qualifier
  def named(name: String): NamedCache = {
    new NamedCacheImpl(name)
  }

  // bind wrapper classes
  def wrapperBindings(cacheApiKey: BindingKey[AsyncCacheApi], namedCache: NamedCache): Seq[Binding[_]] = Seq(
    bind[JavaAsyncCacheApi].qualifiedWith(namedCache).to(new NamedJavaAsyncCacheApiProvider(cacheApiKey)),
    bind[Cached].qualifiedWith(namedCache).to(new NamedCachedProvider(cacheApiKey)),
    bind[SyncCacheApi].qualifiedWith(namedCache).to(new NamedSyncCacheApiProvider(cacheApiKey)),
    bind[JavaSyncCacheApi].qualifiedWith(namedCache).to(new NamedJavaSyncCacheApiProvider(cacheApiKey))
  )

  // bind a cache with the given name
  def bindCache(name: String) = {
    val namedCache = named(name)
    val cacheApiKey = bind[AsyncCacheApi].qualifiedWith(namedCache)
    Seq(
      cacheApiKey.to(new MemcachedCacheApiProvider(name, bind[MemcachedClient], configuration.underlying, environment))
    ) ++ wrapperBindings(cacheApiKey, namedCache)
  }

  def bindDefault[T: ClassTag]: Binding[T] = {
    bind[T].to(bind[T].qualifiedWith(named(defaultCacheName)))
  }

  Seq(
    bind[MemcachedClient].toProvider[MemcachedClientProvider],
    // alias the default cache to the unqualified implementation
    bindDefault[AsyncCacheApi],
    bindDefault[JavaAsyncCacheApi],
    bindDefault[SyncCacheApi],
    bindDefault[JavaSyncCacheApi]
  ) ++ bindCache(defaultCacheName) ++ bindCaches.flatMap(bindCache)
})

private class NamedSyncCacheApiProvider(key: BindingKey[AsyncCacheApi])
    extends Provider[SyncCacheApi] {
  @Inject private var injector: Injector = _
  lazy val get: SyncCacheApi =
    new DefaultSyncCacheApi(injector.instanceOf(key))
}

private class NamedJavaAsyncCacheApiProvider(key: BindingKey[AsyncCacheApi]) extends Provider[JavaAsyncCacheApi] {
  @Inject private var injector: Injector = _
  lazy val get: JavaAsyncCacheApi =
    new JavaDefaultAsyncCacheApi(injector.instanceOf(key))
}

private class NamedJavaSyncCacheApiProvider(key: BindingKey[AsyncCacheApi])
    extends Provider[JavaSyncCacheApi] {
  @Inject private var injector: Injector = _
  lazy val get: JavaSyncCacheApi =
    new JavaDefaultSyncCacheApi(new JavaDefaultAsyncCacheApi(injector.instanceOf(key)))
}

private class NamedCachedProvider(key: BindingKey[AsyncCacheApi]) extends Provider[Cached] {
  @Inject private var injector: Injector = _
  lazy val get: Cached =
    new Cached(injector.instanceOf(key))(injector.instanceOf[Materializer])
}
