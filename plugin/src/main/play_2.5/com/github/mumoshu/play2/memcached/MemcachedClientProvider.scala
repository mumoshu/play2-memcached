package com.github.mumoshu.play2.memcached

import net.spy.memcached.auth.{PlainCallbackHandler, AuthDescriptor}
import play.api.cache.CacheApi
import play.api.{Logger, Configuration, Environment}
import play.api.inject.{ BindingKey, Injector, ApplicationLifecycle }

import scala.concurrent.Future

import javax.inject.{Inject, Singleton, Provider}

import net.spy.memcached.{ConnectionFactoryBuilder, AddrUtil, MemcachedClient}

@Singleton
class MemcachedClientProvider @Inject() (configuration: Configuration, lifecycle: ApplicationLifecycle) extends Provider[MemcachedClient] {
  lazy val logger = Logger("memcached.plugin")
  lazy val get: MemcachedClient = {
    val client = {
      System.setProperty("net.spy.log.LoggerImpl", "com.github.mumoshu.play2.memcached.Slf4JLogger")

      configuration.getString("elasticache.config.endpoint").map { endpoint =>
        new MemcachedClient(AddrUtil.getAddresses(endpoint))
      }.getOrElse {
        lazy val singleHost = configuration.getString("memcached.host").map(AddrUtil.getAddresses)
        lazy val multipleHosts = configuration.getString("memcached.1.host").map { _ =>
          def accumulate(nb: Int): String = {
            configuration.getString("memcached." + nb + ".host").map { h => h + " " + accumulate(nb + 1) }.getOrElse("")
          }
          AddrUtil.getAddresses(accumulate(1))
        }

        val addrs = singleHost.orElse(multipleHosts)
          .getOrElse(throw new RuntimeException("Bad configuration for memcached: missing host(s)"))

        configuration.getString("memcached.user").map { memcacheUser =>
          val memcachePassword = configuration.getString("memcached.password").getOrElse {
            throw new RuntimeException("Bad configuration for memcached: missing password")
          }

          // Use plain SASL to connect to memcached
          val ad = new AuthDescriptor(Array("PLAIN"),
            new PlainCallbackHandler(memcacheUser, memcachePassword))
          val cf = new ConnectionFactoryBuilder()
            .setProtocol(ConnectionFactoryBuilder.Protocol.BINARY)
            .setAuthDescriptor(ad)
            .build()

          new MemcachedClient(cf, addrs)
        }.getOrElse {
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