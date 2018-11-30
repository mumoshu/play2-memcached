package com.github.mumoshu.play2.memcached

import com.typesafe.config.Config
import net.spy.memcached.auth.{PlainCallbackHandler, AuthDescriptor}
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

import javax.inject.{Inject, Singleton, Provider}

import net.spy.memcached.{KetamaConnectionFactory, ConnectionFactoryBuilder, AddrUtil, MemcachedClient, DefaultConnectionFactory}

@Singleton
class MemcachedClientProvider @Inject() (config: Config, lifecycle: ApplicationLifecycle) extends Provider[MemcachedClient] {
  lazy val logger = Logger("memcached.plugin")
  lazy val get: MemcachedClient = {
    val client = {
      System.setProperty("net.spy.log.LoggerImpl", "com.github.mumoshu.play2.memcached.Slf4JLogger")

      if(config.hasPath(MemcachedClientProvider.CONFIG_ELASTICACHE_CONFIG_ENDPOINT)) {
        new MemcachedClient(AddrUtil.getAddresses(config.getString(MemcachedClientProvider.CONFIG_ELASTICACHE_CONFIG_ENDPOINT)))
      } else {
        lazy val singleHost = if(config.hasPath(MemcachedClientProvider.CONFIG_HOST)) {
          Option(AddrUtil.getAddresses(config.getString(MemcachedClientProvider.CONFIG_HOST)))
        } else {
          None
        }
        lazy val multipleHosts = if(config.hasPath("memcached.1.host")) {
          def accumulate(nb: Int): String = {
            if(config.hasPath("memcached." + nb + ".host")) {
              val h = config.getString("memcached." + nb + ".host")
              h + " " + accumulate(nb + 1)
            } else {
              ""
            }
          }
          Option(AddrUtil.getAddresses(accumulate(1).trim()))
        } else {
          None
        }

        val addrs = singleHost.orElse(multipleHosts)
          .getOrElse(throw new RuntimeException("Bad configuration for memcached: missing host(s)"))

        if(config.hasPath(MemcachedClientProvider.CONFIG_USER)) {
          val memcacheUser = config.getString(MemcachedClientProvider.CONFIG_USER)
          val memcachePassword = if(config.hasPath(MemcachedClientProvider.CONFIG_PASSWORD)) {
            config.getString(MemcachedClientProvider.CONFIG_PASSWORD)
          } else {
            throw new RuntimeException("Bad configuration for memcached: missing password")
          }

          // Use plain SASL to connect to memcached
          val ad = new AuthDescriptor(Array("PLAIN"),
            new PlainCallbackHandler(memcacheUser, memcachePassword))
          lazy val consistentHashing = config.getBoolean("memcached.consistentHashing")
          val cf = (if (consistentHashing) new ConnectionFactoryBuilder(new KetamaConnectionFactory()) else new ConnectionFactoryBuilder())
            .setProtocol(ConnectionFactoryBuilder.Protocol.BINARY)
            .setTimeoutExceptionThreshold(
              if(config.hasPath(MemcachedClientProvider.CONFIG_MAX_TIMEOUT_EXCEPTION_THRESHOLD)) {
                config.getInt(MemcachedClientProvider.CONFIG_MAX_TIMEOUT_EXCEPTION_THRESHOLD)
              } else {
                DefaultConnectionFactory.DEFAULT_MAX_TIMEOUTEXCEPTION_THRESHOLD
              })
            .setAuthDescriptor(ad)
            .build()

          new MemcachedClient(cf, addrs)
        } else {
          new MemcachedClient(addrs)
        }
      }
    }

    logger.info("Starting MemcachedPlugin.")

    lifecycle.addStopHook(() => Future.successful {
      logger.info("Stopping MemcachedPlugin.")
      client.shutdown()
    })

    client
  }
}

object MemcachedClientProvider {
  final val CONFIG_ELASTICACHE_CONFIG_ENDPOINT = "elasticache.config.endpoint"
  final val CONFIG_MAX_TIMEOUT_EXCEPTION_THRESHOLD = "memcached.max-timeout-exception-threshold"
  final val CONFIG_HOST = "memcached.host"
  final val CONFIG_USER = "memcached.user"
  final val CONFIG_PASSWORD = "memcached.password"
}
