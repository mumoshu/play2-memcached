package com.github.mumoshu.play2.memcached

import akka.actor.ActorSystem

import play.api.{Configuration, Environment}
import play.api.inject.{BindingKey, Injector}
import play.api.cache.AsyncCacheApi

import javax.inject.{Inject, Singleton, Provider}

import net.spy.memcached.MemcachedClient

import scala.concurrent.ExecutionContext

@Singleton
class MemcachedCacheApiProvider(namespace: String, client: BindingKey[MemcachedClient], configuration: Configuration, environment: Environment) extends Provider[AsyncCacheApi] {
  @Inject private var injector: Injector = _
  @Inject private var actorSystem: ActorSystem = _
  private lazy val ec: ExecutionContext = configuration.get[Option[String]]("play.cache.dispatcher").map(actorSystem.dispatchers.lookup(_)).getOrElse(injector.instanceOf[ExecutionContext])

  lazy val get: AsyncCacheApi = {
    new MemcachedCacheApi(namespace, injector.instanceOf(client), configuration, environment)(ec)
  }
}
