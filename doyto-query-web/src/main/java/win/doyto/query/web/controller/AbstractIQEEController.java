package win.doyto.query.web.controller;

import win.doyto.query.core.Pageable;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * AbstractIQEEController
 *
 * @author f0rb on 2020-01-29
 */
public abstract class AbstractIQEEController<E extends Persistable<I>, I extends Serializable, Q extends Pageable>
        extends AbstractIQRSController<E, I, Q, E, E> {

    protected AbstractIQEEController() {
        super(0);
    }

    @Override
    protected E buildResponse(E e) {
        return e;
    }

    @Override
    protected E buildEntity(E e) {
        return e;
    }

}
