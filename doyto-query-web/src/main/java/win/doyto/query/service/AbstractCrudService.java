package win.doyto.query.service;

import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * AbstractCrudService
 *
 * @author f0rb
 */
public abstract class AbstractCrudService<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
        extends AbstractDynamicService<E, I, Q>
        implements CrudService<E, I, Q> {

}
