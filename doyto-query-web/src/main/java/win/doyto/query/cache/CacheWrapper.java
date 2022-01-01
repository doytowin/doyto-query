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
