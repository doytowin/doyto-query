/*
 * Copyright © 2019-2023 Forb Yuan
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

package win.doyto.query.service;

import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import win.doyto.query.cache.CacheWrapper;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.util.List;

/**
 * CachedDataAccess
 *
 * @author f0rb on 2023/6/16
 * @since 1.0.2
 */
public class CachedDataAccess<E extends Persistable<I>, I extends Serializable, Q>
        implements DataAccess<E, I, Q> {

    @Getter
    private final DataAccess<E, I, Q> delegate;

    protected final CacheWrapper<E> entityCacheWrapper = CacheWrapper.createInstance();
    protected final CacheWrapper<List<E>> queryCacheWrapper = CacheWrapper.createInstance();

    public CachedDataAccess(DataAccess<E, I, Q> dataAccess, CacheManager cacheManager, String cacheName) {
        this.delegate = dataAccess;
        entityCacheWrapper.setCache(cacheManager.getCache(cacheName));
        queryCacheWrapper.setCache(cacheManager.getCache(cacheName + ":query"));
    }

    private String resolveCacheKey(IdWrapper<I> w) {
        return w.toCacheKey();
    }

    private void clearCache() {
        entityCacheWrapper.clear();
        queryCacheWrapper.clear();
    }

    private void evictCache(String key) {
        entityCacheWrapper.evict(key);
        queryCacheWrapper.clear();
    }

    @Override
    public int batchInsert(Iterable<E> entities, String... columns) {
        try {
            return delegate.batchInsert(entities, columns);
        } finally {
            clearCache();
        }
    }

    @Override
    public int patch(E e, Q q) {
        try {
            return delegate.patch(e, q);
        } finally {
            clearCache();
        }
    }

    @Override
    public List<I> queryIds(Q query) {
        return delegate.queryIds(query);
    }

    protected String generateCacheKey(Q query) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            return null;
        }
        return ToStringBuilder.reflectionToString(query, NonNullToStringStyle.NO_CLASS_NAME_NON_NULL_STYLE);
    }

    @Override
    public List<E> query(Q query) {
        String key = generateCacheKey(query);
        return queryCacheWrapper.execute(key, () -> delegate.query(query));
    }

    @Override
    public long count(Q query) {
        return delegate.count(query);
    }

    @Override
    public <V> List<V> queryColumns(Q q, Class<V> clazz, String... columns) {
        return delegate.queryColumns(q, clazz, columns);
    }

    @Override
    public E get(IdWrapper<I> w) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            return delegate.get(w);
        }
        return entityCacheWrapper.execute(resolveCacheKey(w), () -> delegate.get(w));
    }

    @Override
    public int delete(IdWrapper<I> w) {
        try {
            return delegate.delete(w);
        } finally {
            String key = resolveCacheKey(w);
            evictCache(key);
            entityCacheWrapper.execute(key, () -> null);
        }
    }

    @Override
    public int delete(Q query) {
        try {
            return delegate.delete(query);
        } finally {
            clearCache();
        }
    }

    @Override
    public void create(E e) {
        delegate.create(e);
        evictCache(resolveCacheKey(e.toIdWrapper()));
    }

    @Override
    public int update(E e) {
        try {
            return delegate.update(e);
        } finally {
            evictCache(resolveCacheKey(e.toIdWrapper()));
        }
    }

    @Override
    public int patch(E e) {
        try {
            return delegate.patch(e);
        } finally {
            evictCache(resolveCacheKey(e.toIdWrapper()));
        }
    }

    /**
     * NonNullToStringStyle
     *
     * @author f0rb on 2019-07-14
     */
    private static class NonNullToStringStyle extends ToStringStyle {

        public static final NonNullToStringStyle NO_CLASS_NAME_NON_NULL_STYLE = new NonNullToStringStyle(false);

        private NonNullToStringStyle(boolean useClassName) {
            this.setUseClassName(useClassName);
            this.setUseIdentityHashCode(false);
        }

        @Override
        public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
            if (value != null) {
                super.append(buffer, fieldName, value, fullDetail);
            }
        }
    }
}
