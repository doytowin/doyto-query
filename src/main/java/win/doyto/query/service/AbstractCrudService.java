package win.doyto.query.service;

import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * AbstractCrudService
 *
 * @author f0rb
 */
public abstract class AbstractCrudService<E extends Persistable<I>, I extends Serializable, Q extends PageQuery>
    extends AbstractService<E, I, Q> implements CrudService<E, I, Q> {

    @Override
    protected final String resolveCacheKey(E e) {
        return String.valueOf(e.getId());
    }

    @Override
    public E get(I id) {
        return entityCacheWrapper.execute(id.toString(), () -> fetch(id));
    }

    @Override
    public E fetch(I id) {
        return dataAccess.get(id);
    }

    @Override
    public E delete(I id) {
        E e = get(id);
        if (e != null) {
            if (!entityAspects.isEmpty()) {
                transactionOperations.execute(s -> {
                    dataAccess.delete(id);
                    entityAspects.forEach(entityAspect -> entityAspect.afterDelete(e));
                    return null;
                });
            } else {
                dataAccess.delete(id);
            }
            String key = id.toString();
            evictCache(key);
            entityCacheWrapper.execute(key, () -> null);
        }
        return e;
    }

}
