/*
 * Copyright © 2019-2022 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.support.NoOpCache;

import java.util.concurrent.*;

/**
 * CacheUtil
 *
 * @author f0rb on 2020-05-16
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CacheUtil {

    private static final int MAXIMUM_POOL_SIZE = 4;
    private static final ExecutorService executorService = new ThreadPoolExecutor(
            Math.min(Runtime.getRuntime().availableProcessors() / 4 + 1, MAXIMUM_POOL_SIZE), MAXIMUM_POOL_SIZE,
            1, TimeUnit.MINUTES, new SynchronousQueue<>(),
            new RenameThreadFactory(), new ThreadPoolExecutor.DiscardOldestPolicy());

    public static final Cache noOpCache = new NoOpCache("noop");

    @SuppressWarnings("unchecked")
    public static <V> V invoke(Cache cache, Object key, CacheInvoker<V> cacheInvoker) {
        if (cache instanceof NoOpCache || key == null) {
            return cacheInvoker.invoke();
        }
        try {
            Cache.ValueWrapper valueWrapper = cache.get(key);
            if (valueWrapper != null) {
                return (V) valueWrapper.get();
            }
        } catch (Exception e) {
            log.error(String.format("Cache#get failed: [cache=%s, key=%s]", cache.getName(), key), e);
        }
        V value = cacheInvoker.invoke();
        executorService.execute(() -> cache.put(key, value));
        return value;
    }

    private static class RenameThreadFactory implements ThreadFactory {
        private final ThreadFactory delegate = Executors.defaultThreadFactory();

        public Thread newThread(Runnable r) {
            Thread thread = delegate.newThread(r);
            String threadName = thread.getName();
            thread.setName("doyto-cache" + threadName.substring(threadName.lastIndexOf('-')));
            return thread;
        }
    }
}
