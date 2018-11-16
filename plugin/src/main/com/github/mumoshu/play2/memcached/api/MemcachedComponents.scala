package com.github.mumoshu.play2.memcached.api

import play.api.Environment
import play.api.cache.AsyncCacheApi
import play.api.inject.ApplicationLifecycle

import com.github.mumoshu.play2.memcached.{ MemcachedCacheApi, MemcachedClientProvider }
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

/**
 * Memcached components for compile time injection
 */
trait MemcachedComponents {
  def config: Config
  def environment: Environment
  def applicationLifecycle: ApplicationLifecycle
  implicit def executionContext: ExecutionContext

  lazy val memcachedClientProvider: MemcachedClientProvider = new MemcachedClientProvider(config, applicationLifecycle)

  /**
   * Use this to create with the given name.
   */
  def cacheApi(name: String, create: Boolean = true): AsyncCacheApi = {
    new MemcachedCacheApi(name, memcachedClientProvider.get, config, environment)
  }

  lazy val defaultCacheApi: AsyncCacheApi = cacheApi("play")
}
