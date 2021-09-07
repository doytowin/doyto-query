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
        return CacheUtil.invoke(getCache(), key, invocable);
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
