package win.doyto.query.service;

import win.doyto.query.core.IdWrapper;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * CrudService
 *
 * @author f0rb
 */
public interface CrudService<E extends Persistable<I>, I extends Serializable, Q extends PageQuery>
        extends DynamicService<E, I, Q> {

    default E get(I id) {
        return get(IdWrapper.build(id));
    }

    /**
     * force to get a new entity object from database
     *
     * @param id entity id
     * @return a new entity object
     */
    default E fetch(I id) {
        return fetch(IdWrapper.build(id));
    }

    default E delete(I id) {
        return delete(IdWrapper.build(id));
    }

}
