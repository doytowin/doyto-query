package win.doyto.query.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cache.Cache;
import org.springframework.cache.support.NoOpCache;

/**
 * DefaultCacheWrapper
 *
 * @author f0rb
 */
@Getter
@Setter
class DefaultCacheWrapper<V> implements CacheWrapper<V> {

    private Cache cache = new NoOpCache("noop");

}
