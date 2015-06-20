package com.github.mumoshu.play2.memcached

import play.api.cache.CacheApi
import play.api.{Logger, Configuration, Environment}
import play.api.inject.{ BindingKey, Injector, ApplicationLifecycle }

import scala.concurrent.Future

import javax.inject.{Inject, Singleton, Provider}

import net.spy.memcached.MemcachedClient

@Singleton
class MemcachedClientProvider @Inject() (configuration: play.api.Configuration, lifecycle: ApplicationLifecycle) extends Provider[MemcachedClient] {
  lazy val logger = Logger("memcached.plugin")
  lazy val get: MemcachedClient = {
    val client = MemcachedClientFactory.createMemcachedClient(configuration)

    logger.info("Starting MemcachedPlugin.")

    lifecycle.addStopHook(() => Future.successful {
      logger.info("Stopping MemcachedPlugin.")
      client.shutdown()
    })

    client
  }
}