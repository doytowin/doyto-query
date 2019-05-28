package win.doyto.query.service;

import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * CrudService
 *
 * @author f0rb
 */
public interface CrudService<E extends Persistable<I>, I extends Serializable, Q> extends QueryService<E, Q> {

    E get(I id);

    void create(E e);

    void update(E e);

    void patch(E e);

    @Transactional
    default void batchInsert(Iterable<E> entities) {
        if (entities != null) {
            for (E entity : entities) {
                this.create(entity);
            }
        }
    }

    E delete(I id);

    int delete(Q query);
}
