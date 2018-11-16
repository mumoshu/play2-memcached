package com.github.mumoshu.play2.memcached;

import play.Environment;
import play.cache.AsyncCacheApi;
import play.cache.DefaultAsyncCacheApi;
import play.components.AkkaComponents;
import play.components.ConfigurationComponents;
import play.inject.ApplicationLifecycle;

/**
 * Memached Java Components for compile time injection.
 *
 * <p>Usage:</p>
 *
 * <pre>
 * public class MyComponents extends BuiltInComponentsFromContext implements MemcachedComponents {
 *
 *   public MyComponents(ApplicationLoader.Context context) {
 *       super(context);
 *   }
 *
 *   // A service class that depends on cache APIs
 *   public CachedService someService() {
 *       // defaultCacheApi is provided by MemcachedComponents
 *       return new CachedService(defaultCacheApi());
 *   }
 *
 *   // Another service that depends on a specific named cache
 *   public AnotherService someService() {
 *       // cacheApi provided by MemcachedComponents and
 *       // "anotherService" is the name of the cache.
 *       return new CachedService(cacheApi("anotherService"));
 *   }
 *
 *   // other methods
 * }
 * </pre>
 */
public interface MemcachedComponents extends ConfigurationComponents, AkkaComponents {

    Environment environment();

    ApplicationLifecycle applicationLifecycle();

    default MemcachedClientProvider memcachedClientProvider() {
        return new MemcachedClientProvider(
            config(),
            applicationLifecycle().asScala()
        );
    }

    default AsyncCacheApi cacheApi(String name) {
        play.api.cache.AsyncCacheApi scalaAsyncCacheApi = new MemcachedCacheApi(name, memcachedClientProvider().get(), config(), environment().asScala(), executionContext());
        return new DefaultAsyncCacheApi(scalaAsyncCacheApi);
    }

    default AsyncCacheApi defaultCacheApi() {
        return cacheApi("play");
    }
}
