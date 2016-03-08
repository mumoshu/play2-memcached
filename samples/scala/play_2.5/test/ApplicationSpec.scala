import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.cache.CacheApi

import play.api.test._
import play.api.test.Helpers._
import play.cache.NamedCacheImpl

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  sequential

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Your new application is ready.")
    }
  }

  def connectingLocalMemcached[T](block: => T):T = {
    val app = FakeApplication(
      additionalConfiguration = Map(
        "ehcacheplugin" -> "disabled",
        "memcached.host" -> "127.0.0.1:11211",
        "logger.memcached.plugin" -> "DEBUG"
      )
    )

    running(app) {
      app.injector.instanceOf[CacheApi].remove("key")

      val bindingKey = play.api.inject.BindingKey(classOf[CacheApi]).qualifiedWith(new NamedCacheImpl("session-cache"))
      app.injector.instanceOf(bindingKey).remove("key")

      block
    }
  }

  def c(url: String): String = contentAsString(route(FakeRequest(GET, url)).get)

  "The scala sample application" should {

    "return a cached data" in {
      connectingLocalMemcached {
        c("/cache/get") must equalTo ("None")
        c("/cache/set") must equalTo ("Cached.")
        c("/cache/get") must equalTo ("Some(cached value)")
        c("/session/get") must equalTo ("None")
        c("/session/set") must equalTo ("Cached.")
        c("/session/get") must equalTo ("Some(session value)")
        c("/cache/get") must equalTo ("Some(cached value)")
      }
    }

    "return expected results" in {
      connectingLocalMemcached {
        c("/cacheInt") must equalTo ("123class java.lang.Integer 123 int")
        c("/cacheString") must equalTo ("Some(mikoto)")
        c("/cacheBool") must equalTo ("true : class java.lang.Boolean, true : boolean")
      }
    }
  }

}
