package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import cache.Cache

object Application extends Controller {
  

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def set = Action {
    Cache.set("key", "cached value")
    Ok("Cached.")
  }
  
  def get = Action {
    Ok(Cache.get("key").toString)
  }
  
  def delete = Action {
    Cache.set("key", "foooo", 0)
    Ok("Deleted.")
  }

  def cacheInt = Action {
    Cache.set("key", 123)

    Cache.get("key") match {
      case Some(cachedValue) =>
        val intValue = cachedValue.asInstanceOf[Int]
        Ok(cachedValue.toString + cachedValue.getClass + " " + intValue + " " + intValue.getClass)
        // 123 class java.lang.Integer 123 int
    }
  }
  
  def cacheString = Action {
    Cache.set("key", "mikoto")
    
    Ok(Cache.get("key").toString)
    // Some(mikoto)
  }
  
  def cacheBool = Action {
    Cache.set("key", true)
    
    Cache.get("key") match {
      case Some(cachedValue) =>
        val boolValue = cachedValue.asInstanceOf[Boolean]
        Ok(cachedValue.toString + " : " + cachedValue.getClass + ", " + boolValue + " : " + boolValue.getClass)
        // true : class java.lang.Boolean, true : boolean
    }
  }
  
}
