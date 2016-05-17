package enkan.middleware.session;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author kawasima
 */
public class JCacheStore implements KeyValueStore {
    private Cache<String, Serializable> cache;

    public JCacheStore() {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        Configuration<String, Serializable> config = new MutableConfiguration<String, Serializable>()
                .setTypes(String.class, Serializable.class);
        cache = cacheManager.createCache("session", config);
    }

    @Override
    public Serializable read(String key) {
        return cache.get(key);
    }

    @Override
    public String write(String key, Serializable value) {
        if (key == null) {
            key = UUID.randomUUID().toString();
        }
        cache.put(key, value);
        return key;
    }

    @Override
    public String delete(String key) {
        cache.remove(key);
        return key;
    }
}
