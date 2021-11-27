package win.doyto.query.core;

import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.util.List;

/**
 * DataAccess
 *
 * @author f0rb
 */
public interface DataAccess<E extends Persistable<I>, I extends Serializable, Q> {

    List<E> query(Q query);

    long count(Q query);

    <V> List<V> queryColumns(Q q, Class<V> clazz, String... columns);

    default E get(I id) {
        return get(IdWrapper.build(id));
    }

    E get(IdWrapper<I> w);

    default int delete(I id) {
        return delete(IdWrapper.build(id));
    }

    int delete(IdWrapper<I> w);

    int delete(Q query);

    void create(E e);

    default int batchInsert(Iterable<E> entities, String... columns) {
        int count = 0;
        for (E entity : entities) {
            create(entity);
            count++;
        }
        return count;
    }

    int update(E e);

    int patch(E e);

    int patch(E e, Q q);

    List<I> queryIds(Q query);
}
