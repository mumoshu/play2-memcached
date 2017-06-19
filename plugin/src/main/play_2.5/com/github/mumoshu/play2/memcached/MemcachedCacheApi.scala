package com.github.mumoshu.play2.memcached

import java.util.concurrent.TimeUnit

import net.spy.memcached.transcoders.Transcoder
import play.api.cache.CacheApi
import play.api.{Logger, Configuration, Environment}
import play.api.inject.{ BindingKey, Injector, ApplicationLifecycle, Module }

import javax.inject.{Inject, Singleton}

import scala.concurrent.duration.Duration

import net.spy.memcached.MemcachedClient

import scala.reflect.ClassTag

@Singleton
class MemcachedCacheApi @Inject() (val namespace: String, client: MemcachedClient, configuration: Configuration) extends CacheApi {
  lazy val logger = Logger("memcached.plugin")
  lazy val tc = new CustomSerializing().asInstanceOf[Transcoder[Any]]
  lazy val timeout: Int = configuration.getInt("memcached.timeout").getOrElse(1)

  lazy val timeunit: TimeUnit = {
    configuration.getString("memcached.timeunit").getOrElse("seconds") match {
      case "seconds" => TimeUnit.SECONDS
      case "milliseconds" => TimeUnit.MILLISECONDS
      case "microseconds" => TimeUnit.MICROSECONDS
      case "nanoseconds" => TimeUnit.NANOSECONDS
      case _ => TimeUnit.SECONDS
    }
  }
  def get[T: ClassTag](key: String): Option[T] =
    if (key.isEmpty) {
      None
    } else {
      val ct = implicitly[ClassTag[T]]

      logger.debug("Getting the cached for key " + namespace + key)
      val future = client.asyncGet(namespace + key, tc)
      try {
        val any = future.get(timeout, timeunit)
        if (any != null) {
          logger.debug("any is " + any.getClass)
        }

        Option(
          any match {
            case x if ct.runtimeClass.isInstance(x) => x.asInstanceOf[T]
            case x if ct == ClassTag.Nothing => x.asInstanceOf[T]
            case x => x.asInstanceOf[T]
          }
        )
      } catch {
        case e: Throwable =>
          logger.error("An error has occured while getting the value from memcached. ct=" + ct , e)
          future.cancel(false)
          None
      }
    }

  def getOrElse[A: ClassTag](key: String, expiration: Duration = Duration.Inf)(orElse: => A): A = {
    get[A](key).getOrElse {
      val value = orElse
      set(key, value, expiration)
      value
    }
  }

  def set(key: String, value: Any, expiration: Duration = Duration.Inf) {
    if (!key.isEmpty) {
      val exp = if (expiration.isFinite()) expiration.toSeconds.toInt else 0
      client.set(namespace + key, exp, value, tc)
    }
  }

  def remove(key: String) {
    if (!key.isEmpty) {
      client.delete(namespace + key)
    }
  }
}

object MemcachedCacheApi {
  object ValFromJavaObject {
    def unapply(x: AnyRef): Option[AnyVal] = x match {
      case x: java.lang.Byte => Some(x.byteValue())
      case x: java.lang.Short => Some(x.shortValue())
      case x: java.lang.Integer => Some(x.intValue())
      case x: java.lang.Long => Some(x.longValue())
      case x: java.lang.Float => Some(x.floatValue())
      case x: java.lang.Double => Some(x.doubleValue())
      case x: java.lang.Character => Some(x.charValue())
      case x: java.lang.Boolean => Some(x.booleanValue())
      case _ => None
    }
  }
}
