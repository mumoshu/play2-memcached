package controllers;

import play.*;
import play.cache.Cache;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {
  
  public static Result index() {
    return ok(index.render("Your new application is ready."));
  }

    public static Result set() {
        Cache.set("key", "cached value");
        return ok("Cached.");
    }

    public static Result get() {
        return ok("Cached value = " + Cache.get("key"));
    }

    public static Result delete() {
        Cache.set("key", "foooo", 0);
        return ok("Deleted.");
    }

    public static Result cacheInt() {
        Cache.set("key", new Integer(123));

        Object cachedValue = Cache.get("key");

        if (cachedValue != null) {
            Integer integerValue = (Integer)cachedValue;
            return ok(cachedValue.toString() + cachedValue.getClass() + " " + integerValue + " " + integerValue.getClass());
            // 123 class java.lang.Integer 123 int
        } else {
            throw new RuntimeException("Could not get the cached value.");
        }
    }

    public static Result cacheString() {
        Cache.set("key", "mikoto");

        return ok(Cache.get("key").toString());
        // Some(mikoto)
    }

    public static Result cacheBool() {
        Cache.set("key", new Boolean(true));

        Object cachedValue = Cache.get("key");

        if (cachedValue != null) {
            Boolean booleanValue = (Boolean)cachedValue;
            return ok(cachedValue.toString() + " : " + cachedValue.getClass() + ", " + booleanValue + " : " + booleanValue.getClass());
            // true : class java.lang.Boolean, true : boolean
        } else {
            throw new RuntimeException("Could not get the cached value.");
        }
    }

}
