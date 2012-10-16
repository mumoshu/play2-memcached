import com.github.mumoshu.play2.memcached.MemcachedPlugin
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
    }

    "has an another way to remove the stored value" in new defaultContext {

      Cache.set(key, "foo")

      current.plugin[MemcachedPlugin].foreach(_.api.remove(key))

      Cache.get(key) must beEqualTo (None)
    }
  }

}
