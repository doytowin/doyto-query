package win.doyto.query.core;

import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.entity.Persistable;
import win.doyto.query.entity.UserIdProvider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AbstractCrudService
 *
 * @author f0rb
 * @date 2019-05-14
 */
public abstract class AbstractCrudService<E extends Persistable<I>, I extends Serializable, Q> implements QueryService<E, Q> {

    @Delegate
    protected DataAccess<E, I, Q> dataAccess;

    @Autowired(required = false)
    protected UserIdProvider userIdProvider;

    public AbstractCrudService(DataAccess<E, I, Q> dataAccess) {
        this.dataAccess = dataAccess;
    }

    public E save(E e) {
        if (userIdProvider != null) {
            userIdProvider.setupUserId(e);
        }
        if (e.isNew()) {
            dataAccess.create(e);
        } else {
            dataAccess.update(e);
        }
        return e;
    }

    @Transactional
    public List<E> save(Iterable<E> entities) {
        List<E> result = new ArrayList<>();
        if (entities == null) {
            return result;
        } else {
            for (E entity : entities) {
                result.add(this.save(entity));
            }
            return result;
        }
    }
}
