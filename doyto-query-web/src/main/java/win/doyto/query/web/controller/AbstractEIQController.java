package win.doyto.query.web.controller;

import win.doyto.query.core.Pageable;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.util.List;

/**
 * AbstractIQEEController
 *
 * @author f0rb on 2020-01-29
 */
public abstract class AbstractEIQController<E extends Persistable<I>, I extends Serializable, Q extends Pageable>
        extends AbstractRestController<E, I, Q, E, E> {

    @Override
    public List<E> query(Q q) {
        return service.query(q);
    }
}
