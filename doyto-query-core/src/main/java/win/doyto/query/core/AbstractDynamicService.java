package win.doyto.query.core;

import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * AbstractDynamicService
 *
 * @author f0rb on 2019-05-26
 */
public abstract class AbstractDynamicService<E extends Persistable<I>, I extends Serializable, Q>
    extends AbstractCrudService<E, I, Q> {

    @Override
    public E get(I id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E delete(I id) {
        throw new UnsupportedOperationException();
    }

    public E get(E e) {
        return entityCacheWrapper.execute(e.getId(), () -> dataAccess.get(e));
    }

    public E delete(E entity) {
        E e = get(entity);
        if (e != null) {
            dataAccess.delete(entity);
            entityCacheWrapper.execute(e.getId(), () -> null);
            entityAspects.forEach(entityAspect -> entityAspect.afterDelete(e));
        }
        return e;
    }
}
