package win.doyto.query.cache;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import win.doyto.query.config.GlobalConfiguration;

/**
 * DefaultCacheWrapper
 *
 * @author f0rb
 */
@Slf4j
@Getter
class DefaultCacheWrapper<V> implements CacheWrapper<V> {

    private Cache cache = CacheUtil.noOpCache;

    @Override
    public void setCache(Cache cache) {
        this.cache = GlobalConfiguration.instance().isIgnoreCacheException() ? CacheProxy.wrap(cache) : cache;
    }

}
