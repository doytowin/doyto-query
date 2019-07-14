package win.doyto.query.service;

import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * AbstractDynamicService
 *
 * @author f0rb on 2019-05-26
 */
@SuppressWarnings("squid:S00112")
public abstract class AbstractDynamicService<E extends Persistable<I>, I extends Serializable, Q extends PageQuery>
    extends AbstractService<E, I, Q> implements DynamicService<E, I, Q> {

    @Override
    public E get(E param) {
        return entityCacheWrapper.execute(resolveCacheKey(param), () -> fetch(param));
    }

    @Override
    public E fetch(E param) {
        return dataAccess.get(param);
    }

    @Override
    public E delete(E param) {
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
            evictCache(key);
            entityCacheWrapper.execute(key, () -> null);
        }
        return e;
    }

}
