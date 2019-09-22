package win.doyto.query.cache;

import org.springframework.cache.Cache;
import win.doyto.query.core.Invocable;

/**
 * CacheWrapper
 *
 * @author f0rb
 */
public interface CacheWrapper<T> {

    static <V> DefaultCacheWrapper<V> createInstance() {
        return new DefaultCacheWrapper<>();
    }

    default T execute(String key, Invocable<T> invocable) {
        return DefaultCacheWrapper.invoke(getCache(), key, invocable);
    }

    void setCache(Cache cache);

    Cache getCache();

    @SuppressWarnings("unchecked")
    default T get(String key) {
        Cache.ValueWrapper valueWrapper = getCache().get(key);
        return valueWrapper != null ? (T) valueWrapper.get() : null;
    }

    default void evict(String key) {
        getCache().evict(key);
    }

    default void clear() {
        getCache().clear();
    }
}
