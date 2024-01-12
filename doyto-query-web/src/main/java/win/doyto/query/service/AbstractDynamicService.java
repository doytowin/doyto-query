/*
 * Copyright © 2019-2024 Forb Yuan
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

import jakarta.annotation.Resource;
import lombok.Setter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import win.doyto.query.annotation.Entity;
import win.doyto.query.annotation.EntityType;
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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * AbstractDynamicService
 *
 * @author f0rb on 2019-05-28
 */
@SuppressWarnings("java:S6813")
public abstract class AbstractDynamicService<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
        implements DynamicService<E, I, Q>, InitializingBean {

    protected DataAccess<E, I, Q> dataAccess;

    protected final Class<E> entityClass;

    @Autowired(required = false)
    private UserIdProvider<?> userIdProvider;

    @Setter
    @Autowired(required = false)
    private CacheManager cacheManager;

    @Lazy
    @Autowired(required = false)
    protected List<EntityAspect<E>> entityAspects = new LinkedList<>();

    protected TransactionOperations transactionOperations = NoneTransactionOperations.instance;
    private List<String> cacheList;

    @Resource
    private BeanFactory beanFactory;

    @SuppressWarnings("unchecked")
    protected AbstractDynamicService() {
        entityClass = (Class<E>) BeanUtil.getActualTypeArguments(getConcreteClass())[0];
        dataAccess = new MemoryDataAccess<>(entityClass);
    }

    protected Class<?> getConcreteClass() {
        return getClass();
    }

    @Autowired(required = false)
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        transactionOperations = new TransactionTemplate(transactionManager);
    }

    @Value("${doyto.query.caches:}")
    public void setCacheList(String caches) {
        cacheList = Arrays.stream(caches.split("[,\\s]")).filter(s -> !s.isEmpty()).toList();
    }

    protected String getCacheName() {
        return entityClass.getSimpleName().intern();
    }

    private EntityType getEntityType() {
        Entity entity = entityClass.getAnnotation(Entity.class);
        return entity != null ? entity.type() : EntityType.RELATIONAL;
    }

    @SuppressWarnings({"java:S4973", "StringEquality"})
    @Override
    public void afterPropertiesSet() {
        try {
            if (beanFactory != null) {
                EntityType entityType = getEntityType();
                dataAccess = DataAccessManager.create(entityType, beanFactory, entityClass);
            }
            if (!entityAspects.isEmpty()) {
                dataAccess = new AspectDataAccess<>(dataAccess, entityAspects, transactionOperations);
            }
            if (userIdProvider != null) {
                dataAccess = new UserIdDataAccess<>(dataAccess, userIdProvider);
            }
            if (cacheManager != null) {
                String cacheName = getCacheName();
                if (cacheList.contains(cacheName) || cacheName != entityClass.getSimpleName().intern()) {
                    dataAccess = new CachedDataAccess<>(dataAccess, cacheManager, cacheName);
                }
            }
        } catch (Exception e) {
            throw new BeanInitializationException("Failed to create DataAccess for " + entityClass.getName(), e);
        }
    }

    @Override
    public List<E> query(Q query) {
        return dataAccess.query(query);
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
        dataAccess.create(e);
    }

    public int update(E e) {
        return dataAccess.update(e);
    }

    public int patch(E e) {
        return dataAccess.patch(e);
    }

    @Override
    public int create(Collection<E> entities, String... columns) {
        return dataAccess.batchInsert(entities, columns);
    }

    public int patch(E e, Q q) {
        return dataAccess.patch(e, q);
    }

    public int delete(Q query) {
        return dataAccess.delete(query);
    }

    @Override
    public E get(IdWrapper<I> w) {
        return dataAccess.get(w);
    }

    @Override
    public E fetch(IdWrapper<I> w) {
        if (dataAccess instanceof CachedDataAccess) {
            return ((CachedDataAccess<E, I, Q>) dataAccess).getDelegate().get(w);
        }
        return dataAccess.get(w);
    }

    @Override
    public E delete(IdWrapper<I> w) {
        E e = get(w);
        if (e != null) {
            dataAccess.delete(w);
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
