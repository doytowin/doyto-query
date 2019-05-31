package win.doyto.query.service;

import org.springframework.cache.support.NoOpCache;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AbstractCrudService
 *
 * @author f0rb
 */
public abstract class AbstractCrudService<E extends Persistable<I>, I extends Serializable, Q>
    extends AbstractService<E, I, Q> implements CrudService<E, I, Q> {

    @Override
    public E get(I id) {
        return entityCacheWrapper.execute(id, () -> dataAccess.get(id));
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

    public List<E> query(Q query) {
        if (entityCacheWrapper.getCache() instanceof NoOpCache) {
            return dataAccess.query(query);
        }
        return queryIds(query).stream().map(dataAccess::get).collect(Collectors.toList());
    }

}
