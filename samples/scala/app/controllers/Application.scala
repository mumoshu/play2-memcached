package controllers

import play.api._
import play.api.mvc._
import play.api.cache._
import javax.inject._
import scala.concurrent.duration._

class Application @Inject() (cache: SyncCacheApi, @NamedCache("session-cache") sessionCache: SyncCacheApi, val controllerComponents: ControllerComponents) extends BaseController {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def set = Action {
    cache.set("key", "cached value")
    Ok("Cached.")
  }

  def get = Action {
    Ok(cache.get("key").toString)
  }

  def delete = Action {
    cache.set("key", "foooo", 0.seconds)
    Ok("Deleted.")
  }

  def cacheInt = Action {
    cache.set("key", 123)

    val a = cache.get[Int]("key").get
    val b = cache.get[java.lang.Integer]("key").get
    // 123 class java.lang.Integer 123 int
    val content: String = b.toString + b.getClass + " " + a + " " + a.getClass

    Ok(content)
  }

  def cacheString = Action {
    cache.set("key", "mikoto")

    // Some(mikoto)
    val content: String = cache.get[String]("key").toString

    Ok(content)
  }

  def cacheBool = Action {
    cache.set("key", true)

    val a = cache.get[Boolean]("key").get
    val b = cache.get[java.lang.Boolean]("key").get
    // true : class java.lang.Boolean, true : boolean
    val content: String = b.toString + " : " + b.getClass + ", " + a + " : " + a.getClass

    Ok(content)
  }

  def setSessionCache = Action {
    sessionCache.set("key", "session value")
    Ok("Cached.")
  }

  def getSessionCache = Action {
    Ok(sessionCache.get("key").toString)
  }
}
