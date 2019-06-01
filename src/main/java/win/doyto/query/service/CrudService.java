package win.doyto.query.service;

import win.doyto.query.core.CommonCrudService;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.util.List;

/**
 * CrudService
 *
 * @author f0rb
 */
public interface CrudService<E extends Persistable<I>, I extends Serializable, Q> extends CommonCrudService<E, Q> {

    E get(I id);

    List<I> queryIds(Q query);

    E delete(I id);

}
