package com.github.mumoshu.play2.memcached

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import net.spy.memcached.auth.{PlainCallbackHandler, AuthDescriptor}
import net.spy.memcached.{ConnectionFactoryBuilder, AddrUtil, MemcachedClient}
import play.api.cache.{CacheAPI, CachePlugin}
import play.api.{Logger, Play, Application}
import scala.util.control.Exception._
import net.spy.memcached.transcoders.{Transcoder, SerializingTranscoder}
import net.spy.memcached.compat.log.{Level, AbstractLogger}

class Slf4JLogger(name: String) extends AbstractLogger(name) {

  val logger = Logger("memcached")

  def isTraceEnabled = logger.isTraceEnabled

  def isDebugEnabled = logger.isDebugEnabled

  def isInfoEnabled = logger.isInfoEnabled

  def log(level: Level, msg: AnyRef, throwable: Throwable) {
    val message = msg.toString
    level match {
      case Level.TRACE => logger.trace(message, throwable)
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

    app.configuration.getString("elasticache.config.endpoint").map { endpoint =>
      new MemcachedClient(AddrUtil.getAddresses(endpoint))
    }.getOrElse {
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
  }

  import java.io._
  
  class CustomSerializing extends SerializingTranscoder{

    // You should not catch exceptions and return nulls here,
    // because you should cancel the future returned by asyncGet() on any exception.
    override protected def deserialize(data: Array[Byte]): java.lang.Object = {
      new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(data)) {
        override protected def resolveClass(desc: ObjectStreamClass) = {
          Class.forName(desc.getName(), false, play.api.Play.current.classloader)
        }
      }.readObject()
    }

    // We don't catch exceptions here to make it corresponding to `deserialize`.
    override protected def serialize(obj: java.lang.Object) = {
      val bos: ByteArrayOutputStream = new ByteArrayOutputStream()
      // Allows serializing `null`.
      // See https://github.com/mumoshu/play2-memcached/issues/7
      new ObjectOutputStream(bos).writeObject(obj)
      bos.toByteArray()
    }
  } 

  lazy val tc = new CustomSerializing().asInstanceOf[Transcoder[Any]]

  lazy val api = new CacheAPI {

    def get(key: String) =
    if (key.isEmpty) {
      None
    } else {
        logger.debug("Getting the cached for key " + namespace + key)
        val future = client.asyncGet(namespace + hash(key), tc)
        try {
          val any = future.get(timeout, timeunit)
          if (any != null) {
            logger.debug("any is " + any.getClass)
          }
          Option(
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
          )
        } catch {
          case e: Throwable =>
            logger.error("An error has occured while getting the value from memcached" , e)
            future.cancel(false)
            None
        }
    }

    def set(key: String, value: Any, expiration: Int) {
      if (!key.isEmpty) {
        client.set(namespace + hash(key), expiration, value, tc)
      }
    }

    def remove(key: String) {
      if (!key.isEmpty) {
        client.delete(namespace + hash(key))
      }
    }
  }
  
  lazy val namespace: String = app.configuration.getString("memcached.namespace").getOrElse("")

  lazy val timeout: Int = app.configuration.getInt("memcached.timeout").getOrElse(1)

  lazy val timeunit: TimeUnit = {
    app.configuration.getString("memcached.timeunit").getOrElse("seconds") match {
      case "seconds" => TimeUnit.SECONDS
      case "milliseconds" => TimeUnit.MILLISECONDS 
      case "microseconds" => TimeUnit.MICROSECONDS
      case "nanoseconds" => TimeUnit.NANOSECONDS
      case _ => TimeUnit.SECONDS
    }
  }

  lazy val hashkeys: String = app.configuration.getString("memcached.hashkeys").getOrElse("off")

  // you may override hash implementation to use more sophisticated hashes, like xxHash for higher performance
  protected def hash(key: String): String = if(hashkeys == "off") key
    else java.security.MessageDigest.getInstance(hashkeys).digest(key.getBytes).map("%02x".format(_)).mkString


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
