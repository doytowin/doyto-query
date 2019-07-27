package win.doyto.query.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.support.NoOpCache;
import win.doyto.query.core.Invocable;

import java.util.concurrent.*;

/**
 * DefaultCacheWrapper
 *
 * @author f0rb
 */
@Slf4j
@Getter
@Setter
class DefaultCacheWrapper<V> implements CacheWrapper<V> {

    private static final int MAXIMUM_POOL_SIZE = 4;
    private static final ExecutorService executorService = new ThreadPoolExecutor(
        Math.min(Runtime.getRuntime().availableProcessors() / 4 + 1, MAXIMUM_POOL_SIZE), MAXIMUM_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
        new RenameThreadFactory(), new ThreadPoolExecutor.DiscardOldestPolicy());

    private Cache cache = new NoOpCache("noop");

    @SuppressWarnings("unchecked")
    public static <V> V invoke(Cache cache, Object key, Invocable<V> invocable) {
        if (cache instanceof NoOpCache || key == null) {
            return invocable.invoke();
        }
        try {
            Cache.ValueWrapper valueWrapper = cache.get(key);
            if (valueWrapper != null) {
                return (V) valueWrapper.get();
            }
        } catch (Exception e) {
            log.error(String.format("Cache#get failed: [cache=%s, key=%s]", cache.getName(), key), e);
        }
        V value = invocable.invoke();
        executorService.execute(() -> {
            try {
                cache.put(key, value);
            } catch (Exception e) {
                log.error(String.format("Cache#put failed: [cache=%s, key=%s]", cache.getName(), key), e);
            }
        });
        return value;
    }

    private static class RenameThreadFactory implements ThreadFactory {
        ThreadFactory delegate = Executors.defaultThreadFactory();

        public Thread newThread(Runnable r) {
            Thread thread = delegate.newThread(r);
            String threadName = thread.getName();
            thread.setName("doyto-cache" + threadName.substring(threadName.lastIndexOf('-')));
            return thread;
        }
    }
}
