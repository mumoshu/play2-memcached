package com.github.mumoshu.play2.memcached

import akka.actor.ActorSystem

import com.typesafe.config.Config
import play.api.Environment
import play.api.inject.{BindingKey, Injector}
import play.api.cache.AsyncCacheApi

import javax.inject.{Inject, Singleton, Provider}

import net.spy.memcached.MemcachedClient

import scala.concurrent.ExecutionContext

@Singleton
class MemcachedCacheApiProvider(namespace: String, client: BindingKey[MemcachedClient], config: Config, environment: Environment) extends Provider[AsyncCacheApi] {
  @Inject private var injector: Injector = _
  @Inject private var actorSystem: ActorSystem = _
  private lazy val ec: ExecutionContext = if(config.hasPath(MemcachedCacheApiProvider.PLAY_CACHE_DISPATCHER)) {
    actorSystem.dispatchers.lookup(config.getString(MemcachedCacheApiProvider.PLAY_CACHE_DISPATCHER))
  } else {
    injector.instanceOf[ExecutionContext]
  }

  lazy val get: AsyncCacheApi = {
    new MemcachedCacheApi(namespace, injector.instanceOf(client), config, environment)(ec)
  }
}

object MemcachedCacheApiProvider {
  final val PLAY_CACHE_DISPATCHER = "play.cache.dispatcher"
}
