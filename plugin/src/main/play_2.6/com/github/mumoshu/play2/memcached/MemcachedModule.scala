package com.github.mumoshu.play2.memcached

import akka.stream.Materializer

import java.util.concurrent.TimeUnit

import net.spy.memcached.transcoders.Transcoder
import play.api.{Configuration, Environment}
import play.api.cache._
import play.api.inject._

import play.cache.{ AsyncCacheApi => JavaAsyncCacheApi, SyncCacheApi => JavaSyncCacheApi, CacheApi => JavaCacheApi, DefaultAsyncCacheApi => JavaDefaultAsyncCacheApi, DefaultSyncCacheApi => JavaDefaultSyncCacheApi, NamedCacheImpl }

import javax.inject.{Inject, Singleton, Provider}

import net.spy.memcached.MemcachedClient

import scala.reflect.ClassTag

class MemcachedModule extends SimpleModule((environment, configuration) => {

  import scala.collection.JavaConverters._

  val defaultCacheName = configuration.underlying.getString("play.modules.cache.defaultCache")
  val bindCaches = configuration.underlying.getStringList("play.modules.cache.bindCaches").asScala

  // Creates a named cache qualifier
  def named(name: String): NamedCache = {
    new NamedCacheImpl(name)
  }

  // bind wrapper classes
  def wrapperBindings(cacheApiKey: BindingKey[AsyncCacheApi], namedCache: NamedCache): Seq[Binding[_]] = Seq(
    bind[JavaAsyncCacheApi].qualifiedWith(namedCache).to(new NamedJavaAsyncCacheApiProvider(cacheApiKey)),
    bind[Cached].qualifiedWith(namedCache).to(new NamedCachedProvider(cacheApiKey)),
    bind[SyncCacheApi].qualifiedWith(namedCache).to(new NamedSyncCacheApiProvider(cacheApiKey)),
    bind[CacheApi].qualifiedWith(namedCache).to(new NamedSyncCacheApiProvider(cacheApiKey)),
    bind[JavaCacheApi].qualifiedWith(namedCache).to(new NamedJavaSyncCacheApiProvider(cacheApiKey)),
    bind[JavaSyncCacheApi].qualifiedWith(namedCache).to(new NamedJavaSyncCacheApiProvider(cacheApiKey))
  )

  // bind a cache with the given name
  def bindCache(name: String) = {
    val namedCache = named(name)
    val cacheApiKey = bind[AsyncCacheApi].qualifiedWith(namedCache)
    Seq(
      cacheApiKey.to(new MemcachedCacheApiProvider(name, bind[MemcachedClient], configuration))
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
    bindDefault[CacheApi],
    bindDefault[JavaCacheApi],
    bindDefault[JavaSyncCacheApi]
  ) ++ bindCache(defaultCacheName) ++ bindCaches.flatMap(bindCache)
})

private class NamedSyncCacheApiProvider(key: BindingKey[AsyncCacheApi])
    extends Provider[SyncCacheApi with CacheApi] {
  @Inject private var injector: Injector = _
  lazy val get: SyncCacheApi with CacheApi =
    new DefaultSyncCacheApi(injector.instanceOf(key))
}

private class NamedJavaAsyncCacheApiProvider(key: BindingKey[AsyncCacheApi]) extends Provider[JavaAsyncCacheApi] {
  @Inject private var injector: Injector = _
  lazy val get: JavaAsyncCacheApi =
    new JavaDefaultAsyncCacheApi(injector.instanceOf(key))
}

private class NamedJavaSyncCacheApiProvider(key: BindingKey[AsyncCacheApi])
    extends Provider[JavaSyncCacheApi with JavaCacheApi] {
  @Inject private var injector: Injector = _
  lazy val get: JavaSyncCacheApi with JavaCacheApi =
    new JavaDefaultSyncCacheApi(new JavaDefaultAsyncCacheApi(injector.instanceOf(key)))
}

private class NamedCachedProvider(key: BindingKey[AsyncCacheApi]) extends Provider[Cached] {
  @Inject private var injector: Injector = _
  lazy val get: Cached =
    new Cached(injector.instanceOf(key))(injector.instanceOf[Materializer])
}
