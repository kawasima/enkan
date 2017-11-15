package enkan.middleware.session;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.spi.CachingProvider;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author kawasima
 */
public class JCacheStore implements KeyValueStore {
    private Cache<String, Serializable> cache;

    public JCacheStore() {
        this(null);
    }

    public JCacheStore(Factory<ExpiryPolicy> expiryPolicyFactory) {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        MutableConfiguration<String, Serializable> config = new MutableConfiguration<String, Serializable>()
                .setTypes(String.class, Serializable.class);
        if (expiryPolicyFactory != null) {
            config.setExpiryPolicyFactory(expiryPolicyFactory);
        }
        cache = cacheManager.getCache("session", String.class, Serializable.class);
        if (cache == null)
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
