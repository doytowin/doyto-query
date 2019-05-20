package win.doyto.query.core;

import java.io.Serializable;
import java.util.List;

/**
 * DataAccess
 *
 * @author f0rb
 */
public interface DataAccess<E, I extends Serializable, Q> {

    List<E> query(Q query);

    long count(Q query);

    E get(I id);

    void delete(I id);

    void create(E e);

    void update(E e);

    /**
     * force to get a new entity object from database
     *
     * @param id entity id
     * @return a new entity object
     */
    E fetch(I id);
}
