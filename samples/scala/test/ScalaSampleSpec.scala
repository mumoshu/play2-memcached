import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

object ScalaSampleSpec extends Specification {

  def connectingLocalMemcached[T](block: => T):T =
    running(
      FakeApplication(
        additionalConfiguration = Map("memcached.host" -> "127.0.0.1:11211")
      )
    )(block)

  def c(url: String): String = contentAsString(routeAndCall(FakeRequest(GET, url)).get)

  "The scala sample application" should {

    "return a cached data" in {
      connectingLocalMemcached {
        c("/cache/get") must equalTo ("None")
        c("/cache/set") must equalTo ("Cached.")
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
