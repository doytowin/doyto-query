package win.doyto.query.cache;

import org.springframework.cache.Cache;

/**
 * CacheWrapper
 *
 * @author f0rb
 */
public interface CacheWrapper<T> {

    static <V> DefaultCacheWrapper<V> createInstance() {
        return new DefaultCacheWrapper<>();
    }

    default T execute(String key, CacheInvoker<T> cacheInvoker) {
        return CacheUtil.invoke(getCache(), key, cacheInvoker);
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
