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

    lazy val ehcache = Play.current.plugin[CachePlugin].map(_.api).getOrElse {
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

    def both[T](block: CacheAPI => T): T = (ehcache :: memcache :: Nil).map(block).last

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

    "keep stored data eternally when `expiration` argument is 0" in new cacheImpls {
      both { api =>
        val value = "theValueShouldRemain"
        api.set(key, value, 0)
        api.get(key) must be some (value)
      }
    }

    "remove keys" in new cacheImpls {
      both { api =>
        api.set(key, "value", 0)
        api.get(key) must be some ("value")
        api.remove(key)
        api.get(key) must be none
      }
    }

    "returns None when getting value for empty key" in new cacheImpls {
      both { api =>
        api.get("") must be none
      }
    }
  }

  "Ehcache implementations of Cache API" should {

    // Until Play 2.2.0, the EhCache plugin had been setting `null` value on `Cache#set(key, null)`.
    // However, since Play 2.2.0, it:
    "remove keys on setting nulls" in new cacheImpls {

      ehcache.set(key, "aa", 0)

      ehcache.get(key) must be some ("aa")

      ehcache.set(key, null, 0)

      ehcache.get(key) must be equalTo None

      ehcache.set(key, "aa", 0)
      ehcache.set(key, null, expiration)

      ehcache.get(key) must be equalTo None
    }

    "store value for empty key" in new cacheImpls {
      ehcache.set("", "aa", 0)
      ehcache.get("") must be none
    }
  }

  "Memcached implementation of CacheAPI" should {

    "remove keys on setting nulls" in new cacheImpls {

      memcache.set(key, "aa", 0)
      memcache.set(key, null, expiration)

      memcache.get(key) must be none

      memcache.set(key, "aa", 0)
      memcache.set(key, null, 0)

      memcache.get(key) must be none
    }

    "not store value for empty key" in new cacheImpls {
      memcache.set("", "aa", 0)
      memcache.get("") must be none

      memcache.remove("")
      memcache.get("") must be none
    }
  }

}
