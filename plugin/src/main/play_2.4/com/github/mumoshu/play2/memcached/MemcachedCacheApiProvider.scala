package com.github.mumoshu.play2.memcached

import play.api.Configuration
import play.api.inject.{BindingKey, Injector}

import javax.inject.{Inject, Singleton, Provider}

import net.spy.memcached.MemcachedClient

@Singleton
class MemcachedCacheApiProvider(namespace: String, client: BindingKey[MemcachedClient], configuration: Configuration) extends Provider[MemcachedCacheApi] {
  @Inject private var injector: Injector = _

  lazy val get: MemcachedCacheApi = {
    new MemcachedCacheApi(namespace, injector.instanceOf(client), configuration)
  }
}
