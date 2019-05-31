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

    public E get(E param) {
        return entityCacheWrapper.execute(param.getId(), () -> dataAccess.get(param));
    }

    public E delete(E param) {
        E e = get(param);
        if (e != null) {
            dataAccess.delete(param);
            entityCacheWrapper.execute(e.getId(), () -> null);
            entityAspects.forEach(entityAspect -> entityAspect.afterDelete(e));
        }
        return e;
    }
}
