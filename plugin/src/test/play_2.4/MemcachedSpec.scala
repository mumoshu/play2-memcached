import net.spy.memcached.MemcachedClient
import org.specs2.mutable._

import com.github.mumoshu.play2.memcached._
import play.api.cache.CacheApi
import play.api.inject.BindingKey
import play.cache.NamedCacheImpl

import scala.collection.JavaConverters._

import play.api.ApplicationLoader

object MemcachedSpec extends Specification {

  sequential

  val memcachedHost = "127.0.0.1:11211"

  val env = play.api.Environment.simple()
  val configMap: Map[String, Object] = Map(
    "play.modules.enabled" -> List("com.github.mumoshu.play2.memcached.MemcachedModule"),
    "play.modules.disabled" -> List("play.api.cache.EhCacheModule"),
    "play.modules.cache.defaultCache" -> "default",
    "play.modules.cache.bindCaches" -> List("secondary"),
    "memcached.1.host" -> memcachedHost
  )
  val config = play.api.Configuration.from(
    configMap
  )
  val lifecycle = new play.api.inject.DefaultApplicationLifecycle
  val modules = play.api.inject.Modules.locate(env, config)

  "play2-memcached" should {
    "provide MemcachedModule" in {
      modules.size must equalTo(1)
    }
  }

  "MemcachedModule" should {
    "provide bindings" in {
      val memcachedModule = modules.head.asInstanceOf[MemcachedModule]

      val bindings = memcachedModule.bindings(env, config)

      bindings.size mustNotEqual (0)
    }

    "provide memcached clients" in {
      val app = play.api.test.FakeApplication(
        additionalConfiguration=configMap
      )

      val memcachedClient = app.injector.instanceOf[MemcachedClient]

      memcachedClient must beAnInstanceOf[MemcachedClient]
    }

    "provide a CacheApi implementation backed by memcached" in {
      val app = play.api.test.FakeApplication(
        additionalConfiguration=configMap
      )

      val cacheApi = app.injector.instanceOf[CacheApi]

      cacheApi must beAnInstanceOf[MemcachedCacheApi]
      cacheApi.asInstanceOf[MemcachedCacheApi].namespace must equalTo ("default")
    }

    "provide a named CacheApi implementation backed by memcached" in {
      val app = play.api.test.FakeApplication(
        additionalConfiguration=configMap
      )

      val cacheApi = app.injector.instanceOf(play.api.inject.BindingKey(classOf[CacheApi]).qualifiedWith(new NamedCacheImpl("secondary")))

      cacheApi must beAnInstanceOf[MemcachedCacheApi]
      cacheApi.asInstanceOf[MemcachedCacheApi].namespace must equalTo ("secondary")
    }
  }
}
