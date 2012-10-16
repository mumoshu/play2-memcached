import com.github.mumoshu.play2.memcached.MemcachedPlugin
import java.io.{PrintWriter, BufferedReader, InputStreamReader}
import java.net.Socket
import org.specs2.specification.Scope
import play.api.cache.Cache
import play.api.Play.current
import play.api.test.{FakeApplication, TestServer}

object MemcachedIntegrationSpec extends ServerIntegrationSpec {

  sequential

  val additionalConfiguration = Map(
    "ehcacheplugin" -> "disabled",
    "memcached.host" -> "127.0.0.1:11211"
  )

  trait defaultContext extends Scope {

    val key = "memcachedIntegrationSpecKey"
    val value = "value"
    val expiration = 1000

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

    "remove keys on setting nulls" in new defaultContext {

      Cache.set(key, value, expiration)
      Cache.get(key) must be some (value)

      Cache.set(key, null)
      Cache.get(key) must be none
    }
  }

  "The CacheAPI implementation of MemcachedPlugin" should {

    "remove keys on setting nulls" in new defaultContext {

      val api = current.plugin[MemcachedPlugin].map(_.api).get

      api.set(key, null, expiration)
      api.get(key) must be none

      getValueLengthViaSocket(key) must not be none
    }

    "store the data when setting expiration time to zero (maybe eternally)" in new defaultContext {

      val api = current.plugin[MemcachedPlugin].map(_.api).get

      api.set(key, value + "*", expiration)
      api.set(key, value, 0)
      api.get(key) must be some (value)
    }

    "provides its own way to remove stored values" in new defaultContext {

      val api = current.plugin[MemcachedPlugin].get.api

      api.set(key, value, expiration)
      api.get(key) should be some(value)
      api.remove(key)
      api.get(key) should be none

      getValueLengthViaSocket(key) must be none
    }

    "has an another way to remove the stored value" in new defaultContext {

      Cache.set(key, "foo")

      current.plugin[MemcachedPlugin].foreach(_.api.remove(key))

      Cache.get(key) must beEqualTo (None)
    }
  }

}
