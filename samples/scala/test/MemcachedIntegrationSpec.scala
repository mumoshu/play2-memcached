import com.github.mumoshu.play2.memcached.MemcachedPlugin
import java.io.{PrintWriter, BufferedReader, InputStreamReader}
import java.net.Socket
import org.specs2.mutable._
import org.specs2.execute.Result
import org.specs2.specification.Scope
import play.api.cache.Cache
import play.api.Play.current
import play.api.test.{FakeApplication, TestServer}
import play.api.test.Helpers._

object MemcachedIntegrationSpec extends Specification {

  sequential

  val additionalConfiguration = Map(
    "ehcacheplugin" -> "disabled",
    "memcached.host" -> "127.0.0.1:11211"
  )

  case class context(additionalConfiguration: Map[String, String] = additionalConfiguration) extends Around {

    val key = "memcachedIntegrationSpecKey"
    val value = "value"
    val expiration = 1000

    override def around[T <% Result](t: => T): Result = {
      running(TestServer(3333, FakeApplication(additionalConfiguration = additionalConfiguration))) {
        t
      }
    }

  }

  lazy val socket = new Socket("127.0.0.1", 11211)
  lazy val input = new BufferedReader(new InputStreamReader(socket.getInputStream))
  lazy val output = new PrintWriter(socket.getOutputStream)

  private def getValueLengthViaSocket(key: String): Option[Int] = {
    output.println("get " + key)
    output.flush()
    var data = ""
    while ({
      val line = input.readLine()
      if (line != null) {
        data += line + "\n"
      }
      line != null && line != "END"
    }) {
      ;
    }

    data.split("\n").head.split(" ") match {
      case Array(_) =>
        None
      case Array(_, _, _, length) =>
        Some(length.toInt)
    }
  }

  "play.api.cache.Cache" should {

    "remove keys on setting nulls" in new context {

      Cache.set(key, value, expiration)
      Cache.get(key) must be some (value)

      Cache.set(key, null)
      Cache.get(key) must be none
    }
  }

  "The CacheAPI implementation of MemcachedPlugin" should {

    "remove keys on setting nulls" in new context {

      val api = current.plugin[MemcachedPlugin].map(_.api).get

      api.set(key, null, expiration)
      api.get(key) must be none

      getValueLengthViaSocket(key) must not be none
    }

    "store the data when setting expiration time to zero (maybe eternally)" in new context {

      val api = current.plugin[MemcachedPlugin].map(_.api).get

      api.set(key, value + "*", expiration)
      api.set(key, value, 0)
      api.get(key) must be some (value)
    }

    "provides its own way to remove stored values" in new context {

      val api = current.plugin[MemcachedPlugin].get.api

      api.set(key, value, expiration)
      api.get(key) should be some(value)
      api.remove(key)
      api.get(key) should be none

      getValueLengthViaSocket(key) must be none
    }

    "has an another way to remove the stored value" in new context {

      Cache.set(key, "foo")

      current.plugin[MemcachedPlugin].foreach(_.api.remove(key))

      Cache.get(key) must beEqualTo (None)
    }

    "has an ability to prefix every key with the global namespace" in new context(additionalConfiguration.updated("memcached.namespace", "mikoto.")) {

      Cache.set(key, "foo")

      Cache.get(key) must be some("foo")

      getValueLengthViaSocket("mikoto." + key) must be some(3)
    }
  }

}
