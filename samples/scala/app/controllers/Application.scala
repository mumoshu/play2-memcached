package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import cache.Cache
import java.util.UUID

object Application extends Controller {
  
  def ActionUsingSession(f: => PlainResult): Action[AnyContent] = {
    ActionUsingSession { request => f }
  }
  
  def ActionUsingSession(f: Request[AnyContent] => PlainResult): Action[AnyContent] = {
    ActionUsingSession[AnyContent](parse.anyContent)(f)
  }

  def ActionUsingSession[A](bp: BodyParser[A])(f: Request[A] => PlainResult): Action[A] = {
    Action(bp) { request: Request[A] =>

      // セッションの有効期間。発行から１時間にします。Play1系ではapplication.confで指定可能で、デフォルトは1時間となっています。
      val maxAge = 3600 * 1000

      // セッションIDや有効期限のキーはPlay 1系に倣ってみます。
      val ID_KEY = "___ID"
      val TS_KEY = "___TS"
      
      def newId = UUID.randomUUID().toString
      def newTimestamp = (System.currentTimeMillis() + maxAge).toString
      
      val sessionData = request.session.get("___TS").map(_.toLong) match {
        case Some(timestamp) if timestamp < System.currentTimeMillis() =>
          Logger.info("セッションが期限切れしているので、生成しなおします。")
          Map(
            ID_KEY -> newId,
            TS_KEY -> newTimestamp
          )
        case _ =>
          Logger.info("セッションを生成または延長します。")
          request.session.data ++ Map(
            ID_KEY -> request.session.get(ID_KEY).getOrElse(newId),
            TS_KEY -> newTimestamp
          )
      }

      val result = f(new Request[A] {
        def headers = request.headers
        def method = request.method
        def path = request.path
        def queryString = request.queryString
        def uri = request.uri
        def body = request.body

        override lazy val session = Session(sessionData)
      })
      
      val cookies = result.header.headers.get(SET_COOKIE).map(Cookies.decode).getOrElse(Seq.empty)
      
      val sessionDataModifiedByAction = cookies.collectFirst {
        case cookie @ Cookie(Session.COOKIE_NAME, value, maxAge, path, domain, secure, httpOnly) =>
          sessionData ++ Session.decodeFromCookie(Some(cookie)).data
      }
      
      result.withSession(Session(sessionDataModifiedByAction.getOrElse(sessionData)))
    }
  }
  
  def index = ActionUsingSession {
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
