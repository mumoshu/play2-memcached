package com.github.mumoshu.play2.memcached

import play.api.cache.{CacheAPI, CachePlugin}
import play.api.{Logger, Play, Application}
import scala.util.control.Exception._

class MemcachedPlugin(app: Application) extends CachePlugin {

  val pluginConfig = Configuration(app.configuration)

  import pluginConfig._

  lazy val client = MemcachedClientFactory.createMemcachedClient(app.configuration)

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
  
  /**
   * Is this plugin enabled.
   *
   * {{{
   * memcachedplugin.disabled=true
   * }}}
   */
  override lazy val enabled = {
    !configuration.getString("memcachedplugin").filter(_ == "disabled").isDefined
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
