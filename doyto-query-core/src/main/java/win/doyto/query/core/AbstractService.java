package win.doyto.query.core;

import lombok.experimental.Delegate;

import java.io.Serializable;

/**
 * AbstractService
 *
 * @author f0rb
 * @date 2019-05-14
 */
public abstract class AbstractService<E, I extends Serializable, Q> implements QueryService<E, Q> {

    @Delegate
    protected DataAccess<E, I, Q> dataAccess;

    public AbstractService(DataAccess<E, I, Q> dataAccess) {
        this.dataAccess = dataAccess;
    }
}
