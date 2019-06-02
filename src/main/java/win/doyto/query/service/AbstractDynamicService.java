package win.doyto.query.service;

import win.doyto.query.core.AbstractService;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * AbstractDynamicService
 *
 * @author f0rb on 2019-05-26
 */
public abstract class AbstractDynamicService<E extends Persistable<I>, I extends Serializable, Q>
    extends AbstractService<E, I, Q> implements DynamicService<E, I, Q> {

    public final E get(E param) {
        return entityCacheWrapper.execute(resolveCacheKey(param), () -> fetch(param));
    }

    @Override
    public final E fetch(E param) {
        return dataAccess.get(param);
    }

    public final E delete(E param) {
        E e = get(param);
        if (e != null) {
            if (!entityAspects.isEmpty()) {
                transactionOperations.execute(s -> {
                    dataAccess.delete(param);
                    entityAspects.forEach(entityAspect -> entityAspect.afterDelete(e));
                    return null;
                });
            } else {
                dataAccess.delete(param);
            }
            String key = resolveCacheKey(e);
            entityCacheWrapper.evict(key);
            entityCacheWrapper.execute(key, () -> null);
        }
        return e;
    }
}
