package win.doyto.query.core;

import java.util.List;

/**
 * DataAccess
 *
 * @author f0rb
 * @date 2019-05-14
 */
public interface DataAccess<E, I, Q> {

    List<E> query(Q query);

    long count(Q query);

    E get(I id);

    void delete(I id);

    void create(E e);

    void update(E e);

}
