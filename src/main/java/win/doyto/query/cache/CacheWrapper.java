package win.doyto.query.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.support.NoOpCache;
import win.doyto.query.core.Invocable;

/**
 * CacheWrapper
 *
 * @author f0rb
 */
public interface CacheWrapper<T> {
    @SuppressWarnings("unchecked")
    static <V> V invoke(Cache cache, Object key, Invocable<V> invocable) {
        if (cache instanceof NoOpCache || key == null) {
            return invocable.invoke();
        }
        Cache.ValueWrapper valueWrapper = cache.get(key);
        if (valueWrapper != null) {
            return (V) valueWrapper.get();
        }
        V value = invocable.invoke();
        cache.put(key, value);
        return value;
    }

    static <V> DefaultCacheWrapper<V> createInstance() {
        return new DefaultCacheWrapper<>();
    }

    default T execute(String key, Invocable<T> invocable) {
        return CacheWrapper.invoke(getCache(), key, invocable);
    }

    void setCache(Cache cache);

    Cache getCache();

    default void evict(String key) {
        getCache().evict(key);
    }

    default void clear() {
        getCache().clear();
    }
}
