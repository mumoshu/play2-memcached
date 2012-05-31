package com.github.mumoshu.play2.memcached

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import net.spy.memcached.auth.{PlainCallbackHandler, AuthDescriptor}
import net.spy.memcached.{ConnectionFactoryBuilder, AddrUtil, MemcachedClient}
import play.api.cache.{CacheAPI, CachePlugin}
import play.api.{Logger, Play, Application}
import util.control.Exception.catching
import net.spy.memcached.transcoders.{Transcoder, SerializingTranscoder}
import net.spy.memcached.compat.log.{Level, AbstractLogger}

class Slf4JLogger(name: String) extends AbstractLogger(name) {

  val logger = Logger("memcached")
  
  def isDebugEnabled = logger.isDebugEnabled

  def isInfoEnabled = logger.isInfoEnabled

  def log(level: Level, msg: AnyRef, throwable: Throwable) {
    val message = msg.toString
    level match {
      case Level.DEBUG => logger.debug(message, throwable)
      case Level.INFO => logger.info(message, throwable)
      case Level.WARN => logger.warn(message, throwable)
      case Level.ERROR => logger.error(message, throwable)
      case Level.FATAL => logger.error("[FATAL] " + message, throwable)
    }
  }
}

class MemcachedPlugin(app: Application) extends CachePlugin {

  lazy val logger = Logger("memcached.plugin")

  lazy val client = {
    System.setProperty("net.spy.log.LoggerImpl", "com.github.mumoshu.play2.memcached.Slf4JLogger")

    lazy val singleHost = app.configuration.getString("memcached.host").map(AddrUtil.getAddresses)
    lazy val multipleHosts = app.configuration.getString("memcached.1.host").map { _ =>
      def accumulate(nb: Int): String = {
        app.configuration.getString("memcached." + nb + ".host").map { h => h + " " + accumulate(nb + 1) }.getOrElse("")
      }
      AddrUtil.getAddresses(accumulate(1))
    }

    val addrs = singleHost.orElse(multipleHosts)
      .getOrElse(throw new RuntimeException("Bad configuration for memcached: missing host(s)"))

    app.configuration.getString("memcached.user").map { memcacheUser =>
      val memcachePassword = app.configuration.getString("memcached.password").getOrElse {
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

  lazy val tc = new SerializingTranscoder().asInstanceOf[Transcoder[Any]]

  lazy val api = new CacheAPI {

    def get(key: String) = {
      Logger.info("Getting the cached for key " + key)
      val future = client.asyncGet(key, tc)
      catching[Any](classOf[Exception]) opt {
        val any = future.get(1, TimeUnit.SECONDS)
        Logger.info("any is " + any.getClass)
        any match {
          case x: java.lang.Byte => x.byteValue()
          case x: java.lang.Short => x.shortValue()
          case x: java.lang.Integer => x.intValue()
          case x: java.lang.Long => x.longValue()
          case x: java.lang.Float => x.floatValue()
          case x: java.lang.Double => x.doubleValue()
          case x: java.lang.Character => x.charValue()
          case x: java.lang.Boolean => x.booleanValue()
          case x => x
        }
      } orElse {
        future.cancel(false)
        None
      }
    }

    def set(key: String, value: Any, expiration: Int) {
      client.set(key, expiration, value, tc)
    }

    def remove(key: String) {
      client.delete(key)
    }
  }

  /**
   * Is this plugin enabled.
   *
   * {{{
   * memcachedplugin.disabled=true
   * }}}
   */
  override lazy val enabled = {
    !app.configuration.getString("memcachedplugin").filter(_ == "disabled").isDefined
  }

  override def onStart() {
    logger.info("Starting MemcachedPlugin.")
    client
  }

  override def onStop() {
    logger.info("Stopping MemcachedPlugin.")
    client.shutdown
    Thread.interrupted()
  }
}
