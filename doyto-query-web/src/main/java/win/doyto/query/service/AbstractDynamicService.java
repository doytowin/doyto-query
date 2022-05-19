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

package win.doyto.query.service;

import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCache;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.*;
import win.doyto.query.annotation.Entity;
import win.doyto.query.annotation.EntityType;
import win.doyto.query.cache.CacheInvoker;
import win.doyto.query.cache.CacheWrapper;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.EntityAspect;
import win.doyto.query.entity.Persistable;
import win.doyto.query.entity.UserIdProvider;
import win.doyto.query.memory.MemoryDataAccess;
import win.doyto.query.util.BeanUtil;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

/**
 * AbstractDynamicService
 *
 * @author f0rb on 2019-05-28
 */
public abstract class AbstractDynamicService<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
        implements DynamicService<E, I, Q> {

    protected DataAccess<E, I, Q> dataAccess;

    protected final Class<E> entityClass;

    protected final CacheWrapper<E> entityCacheWrapper = CacheWrapper.createInstance();
    protected final CacheWrapper<List<E>> queryCacheWrapper = CacheWrapper.createInstance();

    @Autowired(required = false)
    private UserIdProvider<?> userIdProvider = () -> null;

    @Setter
    @Autowired(required = false)
    private CacheManager cacheManager;

    @Lazy
    @Autowired(required = false)
    protected List<EntityAspect<E>> entityAspects = new LinkedList<>();

    protected TransactionOperations transactionOperations = NoneTransactionOperations.instance;

    @SuppressWarnings("unchecked")
    protected AbstractDynamicService() {
        entityClass = (Class<E>) BeanUtil.getActualTypeArguments(getConcreteClass())[0];
        dataAccess = new MemoryDataAccess<>(entityClass);
    }

    protected Class<?> getConcreteClass() {
        return getClass();
    }

    @SuppressWarnings("unchecked")
    @Resource
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        ClassLoader classLoader = beanFactory.getClass().getClassLoader();
        try {
            EntityType entityType = EntityType.RELATIONAL;
            Entity entity = entityClass.getAnnotation(Entity.class);
            if (entity != null) {
                entityType = entity.type();
            }
            if (entityType == EntityType.MONGO_DB) {
                Class<?> mongoDataAccessClass = classLoader.loadClass("win.doyto.query.mongodb.MongoDataAccess");
                tryCreateEmbeddedMongoServerFirst(beanFactory);
                Object mongoClient = beanFactory.getBean("mongo");
                dataAccess = (DataAccess<E, I, Q>) ConstructorUtils.invokeConstructor(mongoDataAccessClass, mongoClient, entityClass);
            } else {// using JdbcDataAccess as default DataAccess
                Class<?> jdbcDataAccessClass = classLoader.loadClass("win.doyto.query.jdbc.JdbcDataAccess");
                Object jdbcOperations = beanFactory.getBean("jdbcTemplate");
                dataAccess = (DataAccess<E, I, Q>) ConstructorUtils.invokeConstructor(jdbcDataAccessClass, jdbcOperations, entityClass);
            }
        } catch (Exception e) {
            throw new BeanInitializationException("Failed to create DataAccess for " + entityClass.getName(), e);
        }
    }

    private void tryCreateEmbeddedMongoServerFirst(BeanFactory beanFactory) {
        try {
            beanFactory.getBean("embeddedMongoServer");
        } catch (BeansException e) {//just ignore when no embeddedMongoServer provided
        }
    }

    @Autowired(required = false)
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        transactionOperations = new TransactionTemplate(transactionManager);
    }

    @SuppressWarnings("java:S4973")
    @Value("${doyto.query.caches:}")
    public void setCacheList(String caches) {
        List<String> cacheList = Arrays.stream(caches.split("[,\\s]")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        if (cacheManager != null) {
            String cacheName = getCacheName();
            if (cacheList.contains(cacheName) || cacheName != entityClass.getSimpleName().intern()) {
                entityCacheWrapper.setCache(cacheManager.getCache(cacheName));
                queryCacheWrapper.setCache(cacheManager.getCache(getQueryCacheName()));
            }
        }
    }

    protected String resolveCacheKey(IdWrapper<I> w) {
        return w.toCacheKey();
    }

    protected String getCacheName() {
        return entityClass.getSimpleName().intern();
    }

    private String getQueryCacheName() {
        return getCacheName() + ":query";
    }

    protected void clearCache() {
        entityCacheWrapper.clear();
        queryCacheWrapper.clear();
    }

    protected void evictCache(String key) {
        entityCacheWrapper.evict(key);
        queryCacheWrapper.clear();
    }

    protected boolean cacheable() {
        return !(entityCacheWrapper.getCache() instanceof NoOpCache);
    }

    @Override
    public List<E> query(Q query) {
        String key = generateCacheKey(query);
        return queryCacheWrapper.execute(key, () -> dataAccess.query(query));
    }

    protected String generateCacheKey(Q query) {
        String key = null;
        if (cacheable() && !TransactionSynchronizationManager.isActualTransactionActive()) {
            key = ToStringBuilder.reflectionToString(query, NonNullToStringStyle.NO_CLASS_NAME_NON_NULL_STYLE);
        }
        return key;
    }

    public long count(Q query) {
        return dataAccess.count(query);
    }

    public List<I> queryIds(Q query) {
        return dataAccess.queryIds(query);
    }

    public <V> List<V> queryColumns(Q query, Class<V> clazz, String... columns) {
        return dataAccess.queryColumns(query, clazz, columns);
    }

    public void create(E e) {
        userIdProvider.setupUserId(e);
        if (!entityAspects.isEmpty()) {
            transactionOperations.execute(s -> {
                dataAccess.create(e);
                entityAspects.forEach(entityAspect -> entityAspect.afterCreate(e));
                return null;
            });
        } else {
            dataAccess.create(e);
        }
        evictCache(resolveCacheKey(e.toIdWrapper()));
    }

    public int update(E e) {
        return doUpdate(e, () -> dataAccess.update(e));
    }

    public int patch(E e) {
        return doUpdate(e, () -> dataAccess.patch(e));
    }

    private int doUpdate(E e, CacheInvoker<Integer> cacheInvoker) {
        userIdProvider.setupUserId(e);
        E origin;
        if (e == null || (origin = dataAccess.get(e.toIdWrapper())) == null) {
            return 0;
        }
        if (!entityAspects.isEmpty()) {
            transactionOperations.execute(s -> {
                cacheInvoker.invoke();
                E current = dataAccess.get(e.toIdWrapper());
                entityAspects.forEach(entityAspect -> entityAspect.afterUpdate(origin, current));
                return null;
            });
        } else {
            cacheInvoker.invoke();
        }
        evictCache(resolveCacheKey(e.toIdWrapper()));
        return 1;
    }

    @Override
    public int create(Iterable<E> entities, String... columns) {
        if (userIdProvider.getUserId() != null) {
            for (E e : entities) {
                userIdProvider.setupUserId(e);
            }
        }
        int insert = dataAccess.batchInsert(entities, columns);
        clearCache();
        return insert;
    }

    public int patch(E e, Q q) {
        userIdProvider.setupUserId(e);
        int patch = dataAccess.patch(e, q);
        clearCache();
        return patch;
    }

    public int delete(Q query) {
        int delete = dataAccess.delete(query);
        clearCache();
        return delete;
    }

    @Override
    public E get(IdWrapper<I> w) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            return fetch(w);
        }
        return entityCacheWrapper.execute(resolveCacheKey(w), () -> fetch(w));
    }

    @Override
    public E fetch(IdWrapper<I> w) {
        return dataAccess.get(w);
    }

    @Override
    public E delete(IdWrapper<I> w) {
        E e = get(w);
        if (e != null) {
            if (!entityAspects.isEmpty()) {
                transactionOperations.execute(s -> {
                    dataAccess.delete(w);
                    entityAspects.forEach(entityAspect -> entityAspect.afterDelete(e));
                    return null;
                });
            } else {
                dataAccess.delete(w);
            }
            String key = resolveCacheKey(w);
            evictCache(key);
            entityCacheWrapper.execute(key, () -> null);
        }
        return e;
    }

    private static class NoneTransactionOperations implements TransactionOperations {
        private static final TransactionOperations instance = new NoneTransactionOperations();
        private static final TransactionStatus TRANSACTION_STATUS = new SimpleTransactionStatus();

        @Override
        public <T> T execute(TransactionCallback<T> transactionCallback) {
            return transactionCallback.doInTransaction(TRANSACTION_STATUS);
        }
    }
}
