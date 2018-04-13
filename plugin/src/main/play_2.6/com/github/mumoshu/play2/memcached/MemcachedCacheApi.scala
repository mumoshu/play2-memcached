package com.github.mumoshu.play2.memcached

import akka.Done

import net.spy.memcached.transcoders.Transcoder
import net.spy.memcached.internal.{ GetCompletionListener, GetFuture, OperationCompletionListener, OperationFuture }
import net.spy.memcached.ops.StatusCode
import play.api.cache.AsyncCacheApi
import play.api.{Environment, Logger, Configuration}

import javax.inject.{Inject, Singleton}

import scala.concurrent.duration.Duration

import net.spy.memcached.MemcachedClient

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.reflect.ClassTag

@Singleton
class MemcachedCacheApi @Inject() (val namespace: String, val client: MemcachedClient, configuration: Configuration, environment: Environment)(implicit context: ExecutionContext) extends AsyncCacheApi {
  lazy val logger = Logger("memcached.plugin")
  lazy val tc = new CustomSerializing(environment.classLoader).asInstanceOf[Transcoder[Any]]
  lazy val hashkeys: String = configuration.getString("memcached.hashkeys").getOrElse("off")
  lazy val throwExceptionFromGetOnError: Boolean = configuration.getBoolean("memcached.throwExceptionFromGetOnError").getOrElse(false)

  def get[T: ClassTag](key: String): Future[Option[T]] = {
    if (key.isEmpty) {
      Future.successful(None)
    } else {
      val ct = implicitly[ClassTag[T]]

      logger.debug("Getting the cache for key " + namespace + key)
      val p = Promise[Option[T]]() // create incomplete promise/future
      client.asyncGet(namespace + hash(key), tc).addListener(new GetCompletionListener() {
        def onComplete(result: GetFuture[_]) {
          try {
            result.getStatus().getStatusCode() match {
              case StatusCode.SUCCESS => {
                val any = result.get
                logger.debug("any is " + any.getClass)
                p.success(Option(
                  any match {
                    case x if ct.runtimeClass.isInstance(x) => x.asInstanceOf[T]
                    case x if ct == ClassTag.Nothing => x.asInstanceOf[T]
                    case x => x.asInstanceOf[T]
                  }
                ))
              }
              case StatusCode.ERR_NOT_FOUND => {
                logger.debug("Cache miss for " + namespace + key)
                p.success(None)
              }
              case _ => {
                fail(result, None)
              }
            }
          } catch {
            case e: Throwable => fail(result, Some(e))
          }
        }

        def fail(result: GetFuture[_], exception: Option[Throwable]): Unit = {
          val msg = "An error has occured while getting the value from memcached. ct=" + ct + ". key=" + key + ". " +
            "spymemcached code: " + result.getStatus().getStatusCode() + " memcached code:" + result.getStatus().getMessage()
          if (throwExceptionFromGetOnError) {
            p.failure(exception.fold(new RuntimeException(msg))(new RuntimeException(msg, _)))
          } else {
            logger.error(msg)
            p.success(None)
          }
        }
      })
      p.future
    }
  }

  def getOrElseUpdate[A: ClassTag](key: String, expiration: Duration)(orElse: => Future[A]): Future[A] = {
    get[A](key).flatMap {
      case Some(value) => Future.successful(value)
      case None => orElse.flatMap(value => set(key, value, expiration).map(_ => value))
    }
  }

  def set(key: String, value: Any, expiration: Duration = Duration.Inf): Future[Done] = {
    if (!key.isEmpty) {
      val p = Promise[Done]() // create incomplete promise/future
      val exp = if (expiration.isFinite()) expiration.toSeconds.toInt else 0
      client.set(namespace + hash(key), exp, value, tc).addListener(new OperationCompletionListener() {
        def onComplete(result: OperationFuture[_]) {
          result.getStatus().getStatusCode() match {
            case StatusCode.SUCCESS => {
              p.success(Done)
            }
            case _ => {
              val msg = "An error has occured while setting the value in memcached. key=" + key + ". value=" + value + ". " +
                "spymemcached code: " + result.getStatus().getStatusCode() + " memcached code:" + result.getStatus().getMessage()
              if (throwExceptionFromGetOnError) {
                p.failure(new RuntimeException(msg))
              } else {
                logger.error(msg)
                p.success(Done)
              }
            }
          }
        }
      })
      p.future
    } else {
      Future.successful(Done)
    }
  }

  def remove(key: String): Future[Done] = {
    if (!key.isEmpty) {
      val p = Promise[Done]() // create incomplete promise/future
      client.delete(namespace + hash(key)).addListener(new OperationCompletionListener() {
        def onComplete(result: OperationFuture[_]) {
          result.getStatus().getStatusCode() match {
            case StatusCode.SUCCESS => {
              p.success(Done)
            }
            case StatusCode.ERR_NOT_FOUND => {
              logger.debug("Cache miss when removing " + namespace + key)
              p.success(Done)
            }
            case _ => {
              val msg = "An error has occured while removing the value in memcached. key=" + key + ". " +
                "spymemcached code: " + result.getStatus().getStatusCode() + " memcached code:" + result.getStatus().getMessage()
              if (throwExceptionFromGetOnError) {
                p.failure(new RuntimeException(msg))
              } else {
                logger.error(msg)
                p.success(Done)
              }
            }
          }
        }
      })
      p.future
    } else {
      Future.successful(Done)
    }
  }

  def removeAll(): Future[Done] = {
    val p = Promise[Done]() // create incomplete promise/future
    client.flush().addListener(new OperationCompletionListener() {
      def onComplete(result: OperationFuture[_]) {
        result.getStatus().getStatusCode() match {
          case StatusCode.SUCCESS => {
            p.success(Done)
          }
          case _ => {
            val msg = "An error has occured while removing all values from memcached. " +
              "spymemcached code: " + result.getStatus().getStatusCode() + " memcached code:" + result.getStatus().getMessage()
            if (throwExceptionFromGetOnError) {
              p.failure(new RuntimeException(msg))
            } else {
              logger.error(msg)
              p.success(Done)
            }
          }
        }
      }
    })
    p.future
  }

  // you may override hash implementation to use more sophisticated hashes, like xxHash for higher performance
  protected def hash(key: String): String = if(hashkeys == "off") key
    else java.security.MessageDigest.getInstance(hashkeys).digest(key.getBytes).map("%02x".format(_)).mkString
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
