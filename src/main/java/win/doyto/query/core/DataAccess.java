package win.doyto.query.core;

import org.springframework.jdbc.core.RowMapper;
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

    E get(I id);

    <V> List<V> queryColumns(Q q, RowMapper<V> rowMapper, String... columns);

    default E get(E e) {
        return get(e.getId());
    }

    int delete(I id);

    default int delete(E e) {
        return delete(e.getId());
    }

    int delete(Q query);

    void create(E e);

    default int batchInsert(Iterable<E> entities) {
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

    /**
     * force to get a new entity object from database
     *
     * @param id entity id
     * @return a new entity object
     */
    E fetch(I id);

    List<I> queryIds(Q query);
}
