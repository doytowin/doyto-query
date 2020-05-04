package win.doyto.query.service;

import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCache;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.*;
import win.doyto.query.cache.CacheWrapper;
import win.doyto.query.core.*;
import win.doyto.query.entity.EntityAspect;
import win.doyto.query.entity.Persistable;
import win.doyto.query.entity.UserIdProvider;
import win.doyto.query.util.BeanUtil;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AbstractDynamicService
 *
 * @author f0rb on 2019-05-28
 */
public abstract class AbstractDynamicService<E extends Persistable<I>, I extends Serializable, Q extends PageQuery>
    implements DynamicService<E, I, Q> {

    protected DataAccess<E, I, Q> dataAccess;

    protected final Class<E> entityClass;

    protected final CacheWrapper<E> entityCacheWrapper = CacheWrapper.createInstance();
    protected final CacheWrapper<List<E>> queryCacheWrapper = CacheWrapper.createInstance();

    @Autowired(required = false)
    private UserIdProvider<?> userIdProvider;

    @Setter
    @Autowired(required = false)
    private CacheManager cacheManager;

    @Lazy
    @Autowired(required = false)
    protected List<EntityAspect<E>> entityAspects = new LinkedList<>();

    protected TransactionOperations transactionOperations = NoneTransactionOperations.instance;

    @SuppressWarnings("unchecked")
    public AbstractDynamicService() {
        entityClass = (Class<E>) BeanUtil.getActualTypeArguments(getClass())[0];
        dataAccess = new MemoryDataAccess<>(entityClass);
    }

    @Autowired
    @SuppressWarnings("unchecked")
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        dataAccess = new JdbcDataAccess<>(jdbcOperations, entityClass, (Class<I>) BeanUtil.getActualTypeArguments(getClass())[1], getRowMapper());
    }

    protected RowMapper<E> getRowMapper() {
        return new BeanPropertyRowMapper<>(entityClass);
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

    protected boolean caching() {
        return !(entityCacheWrapper.getCache() instanceof NoOpCache);
    }

    @Override
    public List<E> query(Q query) {
        String key = null;
        if (caching() && !TransactionSynchronizationManager.isActualTransactionActive()) {
            key = ToStringBuilder.reflectionToString(query, NonNullToStringStyle.NO_CLASS_NAME_NON_NULL_STYLE);
        }
        return queryCacheWrapper.execute(key, () -> dataAccess.query(query));
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
        if (userIdProvider != null) {
            userIdProvider.setupUserId(e);
        }
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

    private int doUpdate(E e, Invocable<Integer> invocable) {
        if (userIdProvider != null) {
            userIdProvider.setupUserId(e);
        }
        E origin;
        if (e == null || (origin = dataAccess.get(e.toIdWrapper())) == null) {
            return 0;
        }
        if (!entityAspects.isEmpty()) {
            transactionOperations.execute(s -> {
                invocable.invoke();
                E current = dataAccess.get(e.toIdWrapper());
                entityAspects.forEach(entityAspect -> entityAspect.afterUpdate(origin, current));
                return null;
            });
        } else {
            invocable.invoke();
        }
        evictCache(resolveCacheKey(e.toIdWrapper()));
        return 1;
    }

    @Override
    public int create(Iterable<E> entities, String... columns) {
        if (userIdProvider != null) {
            for (E e : entities) {
                userIdProvider.setupUserId(e);
            }
        }
        int insert = dataAccess.batchInsert(entities, columns);
        clearCache();
        return insert;
    }

    public int patch(E e, Q q) {
        if (userIdProvider != null) {
            userIdProvider.setupUserId(e);
        }
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
