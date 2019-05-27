package win.doyto.query.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
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
 * AbstractCrudService
 *
 * @author f0rb
 */
public abstract class AbstractCrudService<E extends Persistable<I>, I extends Serializable, Q> implements CrudService<E, I, Q> {

    protected DataAccess<E, I, Q> dataAccess;

    protected CacheWrapper<E> entityCacheWrapper = CacheWrapper.createInstance();

    @Autowired(required = false)
    protected UserIdProvider userIdProvider;

    @Autowired(required = false)
    protected List<EntityAspect<E>> entityAspects = new LinkedList<>();

    protected Class<E> domainType;

    public AbstractCrudService() {
        this.domainType = getDomainType();
        this.dataAccess = new MemoryDataAccess<>(this.domainType);
    }

    @SuppressWarnings("unchecked")
    private Class<E> getDomainType() {
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

    @Override
    public List<E> query(Q query) {
        return dataAccess.query(query);
    }

    @Override
    public long count(Q query) {
        return dataAccess.count(query);
    }

    @Override
    public E get(I id) {
        return entityCacheWrapper.execute(id, () -> dataAccess.get(id));
    }

    @Override
    @Transactional
    public void create(E e) {
        if (userIdProvider != null) {
            userIdProvider.setupUserId(e);
        }
        dataAccess.create(e);
        entityAspects.forEach(entityAspect -> entityAspect.afterCreate(e));
        entityCacheWrapper.evict(e.getId());
    }

    @Override
    @Transactional
    public void update(E e) {
        doUpdate(e, () -> dataAccess.update(e));
    }

    @Override
    @Transactional
    public void patch(E e) {
        doUpdate(e, () -> dataAccess.patch(e));
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

    @Override
    public E delete(I id) {
        E e = get(id);
        if (e != null) {
            dataAccess.delete(id);
            entityCacheWrapper.execute(id, () -> null);
            entityAspects.forEach(entityAspect -> entityAspect.afterDelete(e));
        }
        return e;
    }

}
