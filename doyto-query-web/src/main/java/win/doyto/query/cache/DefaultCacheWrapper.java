/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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
