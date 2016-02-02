package enkan.middleware.session;

import enkan.data.Session;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

/**
 * @author kawasima
 */
public class JCacheStore implements SessionStore {
    private Cache<String, Session> cache;

    public JCacheStore() {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        Configuration<String, Session> config = new MutableConfiguration<String, Session>()
                .setTypes(String.class, Session.class);
        cache = cacheManager.createCache("session", config);
    }

    @Override
    public Session read(String key) {
        return cache.get(key);
    }

    @Override
    public String write(String key, Session session) {
        cache.put(key, session);
        return key;
    }

    @Override
    public String delete(String key) {
        cache.remove(key);
        return key;
    }
}
