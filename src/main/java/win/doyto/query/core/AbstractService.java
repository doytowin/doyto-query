package win.doyto.query.core;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCache;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import win.doyto.query.cache.CacheWrapper;
import win.doyto.query.entity.EntityAspect;
import win.doyto.query.entity.Persistable;
import win.doyto.query.entity.UserIdProvider;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Id;

import static win.doyto.query.core.CrudBuilder.resolveColumn;

/**
 * AbstractService
 *
 * @author f0rb on 2019-05-28
 */
public abstract class AbstractService<E extends Persistable<I>, I extends Serializable, Q>
    implements CommonCrudService<E, Q> {

    private final RowMapper<I> rowMapperForId = new SingleColumnRowMapper<>();

    protected DataAccess<E, I, Q> dataAccess;

    protected final CacheWrapper<E> entityCacheWrapper = CacheWrapper.createInstance();

    @Autowired(required = false)
    private UserIdProvider userIdProvider;

    @Autowired(required = false)
    protected List<EntityAspect<E>> entityAspects = new LinkedList<>();

    protected Class<E> entityType;

    protected TransactionOperations transactionOperations = NoneTransactionOperations.instance;
    private final String idColumn;

    public AbstractService() {
        entityType = getEntityType();
        dataAccess = new MemoryDataAccess<>(entityType);
        Field idField = FieldUtils.getFieldsWithAnnotation(entityType, Id.class)[0];
        idColumn = resolveColumn(idField);
    }

    @SuppressWarnings("unchecked")
    private Class<E> getEntityType() {
        return (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Autowired
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        dataAccess = new JdbcDataAccess<>(jdbcOperations, entityType);
    }

    @Autowired(required = false)
    public void setJdbcOperations(PlatformTransactionManager transactionManager) {
        transactionOperations = new TransactionTemplate(transactionManager);
    }

    @Autowired(required = false)
    public void setCacheManager(CacheManager cacheManager) {
        String cacheName = getCacheName();
        if (cacheName != null) {
            entityCacheWrapper.setCache(cacheManager.getCache(cacheName));
        }
    }

    protected abstract String resolveCacheKey(E e);

    protected String getCacheName() {
        return null;
    }

    public List<E> query(Q query) {
        return dataAccess.query(query);
    }

    public final long count(Q query) {
        return dataAccess.count(query);
    }

    public final List<I> queryIds(Q query) {
        return queryColumns(query, rowMapperForId, idColumn);
    }

    public final <V> List<V> queryColumns(Q query, RowMapper<V> rowMapper, String... columns) {
        return dataAccess.queryColumns(query, rowMapper, columns);
    }

    public final <V> List<V> queryColumns(Q query, Class<V> clazz, String... columns) {
        return queryColumns(query, new BeanPropertyRowMapper<>(clazz), columns);
    }

    public final <S> List<S> queryColumn(Q query, Class<S> clazz, String column) {
        return queryColumns(query, new SingleColumnRowMapper<>(clazz), column);
    }

    public final void create(E e) {
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
        entityCacheWrapper.evict(resolveCacheKey(e));
    }

    public final void update(E e) {
        doUpdate(e, () -> dataAccess.update(e));
    }

    public final void patch(E e) {
        doUpdate(e, () -> dataAccess.patch(e));
    }

    private void doUpdate(E e, Runnable runnable) {
        if (userIdProvider != null) {
            userIdProvider.setupUserId(e);
        }
        if (!entityAspects.isEmpty()) {
            transactionOperations.execute(s -> {
                E origin = dataAccess.fetch(e.getId());
                runnable.run();
                entityAspects.forEach(entityAspect -> entityAspect.afterUpdate(origin, e));
                return null;
            });
        } else {
            runnable.run();
        }
        entityCacheWrapper.evict(resolveCacheKey(e));
    }

    /**
     * 执行<i>INSERT INTO [TABLE] (col1, col2) VALUES (?), (?)</i>
     * <ol>
     * <li><b>会</b>清空全部缓存</li>
     * <li><b>不会</b>按id清理缓存</li>
     * <li><b>不会</b>执行{@link win.doyto.query.entity.EntityAspect#afterCreate(Object)}</li>
     * </ol>
     */
    @Override
    public void batchInsert(Iterable<E> entities) {
        dataAccess.batchInsert(entities);
        entityCacheWrapper.clear();
    }

    public final void patch(E e, Q q) {
        dataAccess.patch(e, q);
        entityCacheWrapper.clear();
    }

    public final int delete(Q query) {
        int delete = dataAccess.delete(query);
        entityCacheWrapper.clear();
        return delete;
    }

    public final boolean exists(Q query) {
        return count(query) > 0;
    }

    protected boolean caching() {
        return !(entityCacheWrapper.getCache() instanceof NoOpCache);
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
