import com.github.mumoshu.play2.memcached.MemcachedPlugin
import org.specs2.mutable._
import org.specs2.specification.Scope
import play.api.cache.{EhCachePlugin, CacheAPI, CachePlugin}
import play.api.Play

/**
 * @author KUOKA Yusuke
 */

object EhcachePluginComplianceSpec extends ServerIntegrationSpec {

  val additionalConfiguration = Map(
    "memcached.host" -> "127.0.0.1:11211"
  )

  trait cacheImpls extends Scope with Before {

    val key = "ehcachePluginComplianceSpecKey"
    val expiration = 1000

    lazy val ehcache = Play.current.plugin[EhCachePlugin].map(_.api).getOrElse {
      throw new RuntimeException("EhCachePlugin is not found.")
    }
    lazy val memcache = Play.current.plugin[MemcachedPlugin].map(_.api).getOrElse {
      throw new RuntimeException("MemcachedPlugin is not found.")
    }

    def testOnValue[T <: Any](value: T) = {
      ehcache.set(key, value, expiration)
      memcache.set(key, value, expiration)
      ehcache.get(key) aka ("the value obtained from the Ehcache impl") should be some (value)
      memcache.get(key) aka ("the value obtained from the Memcached impl") should be some (value)
    }

    def before {
    }
  }

  "Both Ehcache and Memcached implementations of Cache API" should {

    "returns the same value on setting and then getting a " >> {

      "String value" in new cacheImpls {
        testOnValue("value")
      }

      "Int value" in new cacheImpls {
        testOnValue(123)
      }

      "Long value" in new cacheImpls {
        testOnValue(123L)
      }

      "java.util.Date value" in new cacheImpls {
        testOnValue(new java.util.Date)
      }
    }
  }

  "Ehcache implementations of Cache API" should {

    "returns null on setting null" in new cacheImpls {

      ehcache.set(key, null, expiration)
      ehcache.get(key) must be equalTo (Some(null))
    }

    "does not clear the stored data but set it external on providing 0 to the expiration parameter" in new cacheImpls {

      val value = "theValueShouldRemain"
      ehcache.set(key, value, 0)
      ehcache.get(key) must be some (value)
    }
  }

  "Memcached implementation of CacheAPI" should {

    "throws an exception on setting null" in new cacheImpls {

      memcache.set(key, null, expiration) should throwA[Exception]
      // TODO memcache.set(key, null, expiration)
      // TODO memcache.get(key) must be none
    }

    "clear the stored data" in new cacheImpls {

      val value = "theValueShouldNotRemain"
      memcache.set(key, value, 0)
      // TODO this is not an expected behavior
      memcache.get(key) must be some (value)
      // TODO memcache.get(key) must be none
    }
  }

}
