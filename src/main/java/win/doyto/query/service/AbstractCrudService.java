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
        extends AbstractService<E, I, Q>
        implements CrudService<E, I, Q> {

}
