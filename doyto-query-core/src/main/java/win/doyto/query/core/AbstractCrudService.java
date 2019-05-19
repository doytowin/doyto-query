package win.doyto.query.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import win.doyto.query.cache.CacheWrapper;
import win.doyto.query.entity.Persistable;
import win.doyto.query.entity.UserIdProvider;

import java.io.Serializable;
import java.util.List;

/**
 * AbstractCrudService
 *
 * @author f0rb
 * @date 2019-05-14
 */
public abstract class AbstractCrudService<E extends Persistable<I>, I extends Serializable, Q> implements CrudService<E, I, Q> {

    protected DataAccess<E, I, Q> dataAccess;

    protected CacheWrapper<E> entityCacheWrapper = CacheWrapper.createInstance();

    @Autowired(required = false)
    protected UserIdProvider userIdProvider;

    public AbstractCrudService(DataAccess<E, I, Q> dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Autowired(required = false)
    public void setCacheManager(CacheManager cacheManager) {
        String cacheName = getCacheName();
        if (cacheName != null) {
            entityCacheWrapper.setCache(cacheManager.getCache(cacheName));
        }
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

    public E save(E e) {
        if (userIdProvider != null) {
            userIdProvider.setupUserId(e);
        }
        if (e.isNew()) {
            dataAccess.create(e);
        } else {
            dataAccess.update(e);
        }
        entityCacheWrapper.evict(e.getId());
        return e;
    }

    @Override
    public  void delete(I id) {
        dataAccess.delete(id);
        entityCacheWrapper.evict(id);
    }

}
