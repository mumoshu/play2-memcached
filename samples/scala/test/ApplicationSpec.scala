import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.cache.SyncCacheApi

import play.api.Application
import play.api.mvc.Result
import play.api.test._
import play.api.test.Helpers._
import play.cache.NamedCacheImpl
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future

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
      route(this.app, FakeRequest(GET, "/boum")) must beSome[Future[Result]].which (status(_) == NOT_FOUND)
    }

    "render the index page" in new WithApplication(app){
      val home = route(this.app, FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome[String].which(_ == "text/html")
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
      connectingLocalMemcached(this.app) {
        c(this.app, "/cache/get") must equalTo ("None")
        c(this.app, "/cache/set") must equalTo ("Cached.")
        c(this.app, "/cache/get") must equalTo ("Some(cached value)")
        c(this.app, "/session/get") must equalTo ("None")
        c(this.app, "/session/set") must equalTo ("Cached.")
        c(this.app, "/session/get") must equalTo ("Some(session value)")
        c(this.app, "/cache/get") must equalTo ("Some(cached value)")
      }
    }

    "return expected results" in new WithApplication(app){
      connectingLocalMemcached(this.app) {
        c(this.app, "/cacheInt") must equalTo ("123class java.lang.Integer 123 int")
        c(this.app, "/cacheString") must equalTo ("Some(mikoto)")
        c(this.app, "/cacheBool") must equalTo ("true : class java.lang.Boolean, true : boolean")
      }
    }
  }

}
