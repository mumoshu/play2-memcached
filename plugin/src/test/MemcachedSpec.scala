import com.github.mumoshu.play2.memcached._
import net.spy.memcached.MemcachedClient
import org.specs2.mutable._
import play.api.cache.AsyncCacheApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.cache.NamedCacheImpl
import play.api.test.WithApplication

import scala.jdk.CollectionConverters._

object MemcachedSpec extends Specification {

  sequential

  val memcachedHost = "127.0.0.1:11211"

  val environment = play.api.Environment.simple()

  val configurationMap: Map[String, Object] = Map(
    "play.modules.enabled" -> List(
      "play.api.i18n.I18nModule",
      "play.api.mvc.CookiesModule",
      "com.github.mumoshu.play2.memcached.MemcachedModule",
      "play.api.inject.BuiltinModule"
    ).asJava,
    "play.allowGlobalApplication" -> "false",
    "play.cache.defaultCache" -> "default",
    "play.cache.bindCaches" -> List("secondary").asJava,
    "memcached.1.host" -> memcachedHost
  )
  val configuration = play.api.Configuration.from(
    configurationMap
  )

  val modules = play.api.inject.Modules.locate(environment, configuration)

  "play2-memcached" should {
    "provide MemcachedModule" in {
      (modules.find { module => module.isInstanceOf[MemcachedModule] }.get).asInstanceOf[MemcachedModule] must beAnInstanceOf[MemcachedModule]
    }
  }

  "Module" should {
    "provide bindings" in {
      val memcachedModule = (modules.find { module => module.isInstanceOf[MemcachedModule] }.get).asInstanceOf[MemcachedModule]

      val bindings = memcachedModule.bindings(environment, configuration)

      bindings.size mustNotEqual (0)
    }
  }

  "Injector" should {

    def app = GuiceApplicationBuilder().configure(configurationMap).build()

    "provide memcached clients" in new WithApplication(app) {
      val memcachedClient = app.injector.instanceOf(play.api.inject.BindingKey(classOf[MemcachedClient]))

      memcachedClient must beAnInstanceOf[MemcachedClient]
    }

    "provide a CacheApi implementation backed by memcached" in new WithApplication(app) {
      val cacheApi = app.injector.instanceOf(play.api.inject.BindingKey(classOf[AsyncCacheApi]))

      cacheApi must beAnInstanceOf[MemcachedCacheApi]
      cacheApi.asInstanceOf[MemcachedCacheApi].namespace must equalTo ("default")
    }

    "provide a named CacheApi implementation backed by memcached" in new WithApplication(app) {
      val cacheApi = app.injector.instanceOf(play.api.inject.BindingKey(classOf[AsyncCacheApi]).qualifiedWith(new NamedCacheImpl("secondary")))

      cacheApi must beAnInstanceOf[MemcachedCacheApi]
      cacheApi.asInstanceOf[MemcachedCacheApi].namespace must equalTo ("secondary")
    }
  }
}
