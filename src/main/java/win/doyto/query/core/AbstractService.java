package win.doyto.query.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.cache.CacheWrapper;
import win.doyto.query.entity.EntityAspect;
import win.doyto.query.entity.Persistable;
import win.doyto.query.entity.UserIdProvider;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;

/**
 * AbstractService
 *
 * @author f0rb on 2019-05-28
 */
public abstract class AbstractService<E extends Persistable<I>, I extends Serializable, Q>
    implements CommonCrudService<E, Q> {

    private final RowMapper<I> rowMapperForId = new SingleColumnRowMapper<>();

    protected DataAccess<E, I, Q> dataAccess;

    protected CacheWrapper<E> entityCacheWrapper = CacheWrapper.createInstance();

    @Autowired(required = false)
    protected UserIdProvider userIdProvider;

    @Autowired(required = false)
    protected List<EntityAspect<E>> entityAspects = new LinkedList<>();

    protected Class<E> domainType;

    public AbstractService() {
        this.domainType = getDomainType();
        this.dataAccess = new MemoryDataAccess<>(this.domainType);
    }

    @SuppressWarnings("unchecked")
    protected Class<E> getDomainType() {
        return (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Autowired(required = false)
    public void setCacheManager(CacheManager cacheManager) {
        String cacheName = getCacheName();
        if (cacheName != null) {
            entityCacheWrapper.setCache(cacheManager.getCache(cacheName));
        }
    }

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        dataAccess = new JdbcDataAccess<>(jdbcTemplate, domainType);
    }

    protected String getCacheName() {
        return null;
    }

    public List<E> query(Q query) {
        return dataAccess.query(query);
    }

    public long count(Q query) {
        return dataAccess.count(query);
    }

    public List<I> queryIds(Q query) {
        return queryColumns(query, rowMapperForId, "id");
    }

    public <V> List<V> queryColumns(Q query, RowMapper<V> rowMapper, String... columns) {
        return dataAccess.queryColumns(query, rowMapper, columns);
    }

    public <V> List<V> queryColumns(Q query, Class<V> clazz, String... columns) {
        return queryColumns(query, new BeanPropertyRowMapper<>(clazz), columns);
    }

    public <S> List<S> queryColumn(Q query, Class<S> clazz, String column) {
        return queryColumns(query, new SingleColumnRowMapper<>(clazz), column);
    }

    @Transactional
    public void create(E e) {
        if (userIdProvider != null) {
            userIdProvider.setupUserId(e);
        }
        dataAccess.create(e);
        entityAspects.forEach(entityAspect -> entityAspect.afterCreate(e));
        entityCacheWrapper.evict(e.getId());
    }

    @Transactional
    public void update(E e) {
        doUpdate(e, () -> dataAccess.update(e));
    }

    @Transactional
    public void patch(E e) {
        doUpdate(e, () -> dataAccess.patch(e));
    }

    @Transactional
    public void patch(E e, Q q) {
        dataAccess.patch(e, q);
        entityCacheWrapper.clear();
    }

    private void doUpdate(E e, Runnable runnable) {
        if (userIdProvider != null) {
            userIdProvider.setupUserId(e);
        }
        E origin = entityAspects.isEmpty() ? null : dataAccess.fetch(e.getId());
        runnable.run();
        entityAspects.forEach(entityAspect -> entityAspect.afterUpdate(origin, e));
        entityCacheWrapper.evict(e.getId());
    }

    public int delete(Q query) {
        int delete = dataAccess.delete(query);
        entityCacheWrapper.clear();
        return delete;
    }

    public boolean exists(Q query) {
        return count(query) > 0;
    }
}
