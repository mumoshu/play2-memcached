import net.spy.memcached.MemcachedClient
import org.specs2.mutable._
import play.api.test._

import com.github.mumoshu.play2.memcached._
import play.api.cache.CacheApi
import play.api.inject._
import play.cache.NamedCacheImpl
import play.test.WithApplication

import scala.collection.JavaConverters._

import play.api.{Configuration, Environment, ApplicationLoader}

object MemcachedSpec extends Specification {

  sequential

  val memcachedHost = "127.0.0.1:11211"

  val environment = play.api.Environment.simple()

  val configurationMap: Map[String, Object] = Map(
    "play.modules.enabled" -> List(
      "com.github.mumoshu.play2.memcached.MemcachedModule",
      "play.api.inject.BuiltinModule"
    ),
    "play.modules.disabled" -> List("play.api.cache.EhCacheModule"),
    "play.modules.cache.defaultCache" -> "default",
    "play.modules.cache.bindCaches" -> List("secondary"),
    "memcached.1.host" -> memcachedHost
  )
  val configuration = play.api.Configuration.from(
    configurationMap
  )

  val modules = play.api.inject.Modules.locate(environment, configuration)

  "play2-memcached" should {
    "provide MemcachedModule" in {
      (modules.find { module => module.isInstanceOf[MemcachedModule] }.get) must beAnInstanceOf[MemcachedModule]
    }
  }

  "Module" should {
    "provide bindings" in {
      val memcachedModule = modules.head.asInstanceOf[MemcachedModule]

      val bindings = memcachedModule.bindings(environment, configuration)

      bindings.size mustNotEqual (0)
    }
  }

  "Injector" should {
    import play.api.inject.bind

    def app = play.api.test.FakeApplication(
      additionalConfiguration = configurationMap
    )

    def injector = app.injector

    "provide memcached clients" in {
      val memcachedClient = injector.instanceOf[MemcachedClient]

      memcachedClient must beAnInstanceOf[MemcachedClient]
    }

    "provide a CacheApi implementation backed by memcached" in {
      val cacheApi = injector.instanceOf[CacheApi]

      cacheApi must beAnInstanceOf[MemcachedCacheApi]
      cacheApi.asInstanceOf[MemcachedCacheApi].namespace must equalTo ("default")
    }

    "provide a named CacheApi implementation backed by memcached" in {
      val cacheApi = injector.instanceOf(play.api.inject.BindingKey(classOf[CacheApi]).qualifiedWith(new NamedCacheImpl("secondary")))

      cacheApi must beAnInstanceOf[MemcachedCacheApi]
      cacheApi.asInstanceOf[MemcachedCacheApi].namespace must equalTo ("secondary")
    }
  }
}
