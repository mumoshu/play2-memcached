import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.cache.SyncCacheApi

import play.api.Application
import play.api.test._
import play.api.test.Helpers._
import play.cache.NamedCacheImpl
import play.api.inject.guice.GuiceApplicationBuilder

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  sequential

  def app = GuiceApplicationBuilder().configure(Map(
      "memcached.host" -> "127.0.0.1:11211"
    )).build()

  "Application" should {

    "send 404 on a bad request" in new WithApplication(app){
      route(app, FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
    }

    "render the index page" in new WithApplication(app){
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Your new application is ready.")
    }
  }

  def connectingLocalMemcached[T](app: Application)(block: => T):T = {

      app.injector.instanceOf[SyncCacheApi].remove("key")

      val bindingKey = play.api.inject.BindingKey(classOf[SyncCacheApi]).qualifiedWith(new NamedCacheImpl("session-cache"))
      app.injector.instanceOf(bindingKey).remove("key")

      block
  }

  def c(app: Application, url: String): String = contentAsString(route(app, FakeRequest(GET, url)).get)

  "The scala sample application" should {

    "return a cached data" in new WithApplication(app){
      connectingLocalMemcached(app) {
        c(app, "/cache/get") must equalTo ("None")
        c(app, "/cache/set") must equalTo ("Cached.")
        c(app, "/cache/get") must equalTo ("Some(cached value)")
        c(app, "/session/get") must equalTo ("None")
        c(app, "/session/set") must equalTo ("Cached.")
        c(app, "/session/get") must equalTo ("Some(session value)")
        c(app, "/cache/get") must equalTo ("Some(cached value)")
      }
    }

    "return expected results" in new WithApplication(app){
      connectingLocalMemcached(app) {
        c(app, "/cacheInt") must equalTo ("123class java.lang.Integer 123 int")
        c(app, "/cacheString") must equalTo ("Some(mikoto)")
        c(app, "/cacheBool") must equalTo ("true : class java.lang.Boolean, true : boolean")
      }
    }
  }

}
