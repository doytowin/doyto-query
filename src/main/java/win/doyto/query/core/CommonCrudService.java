package win.doyto.query.core;

import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.QueryService;

/**
 * CommonCrudService
 *
 * @author f0rb on 2019-06-01
 */
public interface CommonCrudService<E extends Persistable, Q> extends QueryService<E, Q> {

    void create(E e);

    void update(E e);

    default E save(E e) {
        if (e.isNew()) {
            create(e);
        } else {
            update(e);
        }
        return e;
    }

    void patch(E e);

    @Transactional
    default void batchInsert(Iterable<E> entities) {
        for (E entity : entities) {
            create(entity);
        }
    }

    /**
     * 执行<i>UPDATE [TABLE] SET ... WHERE ...</i>
     * <ol>
     * <li><b>会</b>清空全部缓存</li>
     * <li><b>不会</b>按id清理缓存</li>
     * <li><b>不会</b>执行{@link win.doyto.query.entity.EntityAspect#afterUpdate(Object, Object)}</li>
     * </ol>
     *
     * @param e entity object
     * @param q query object
     */
    void patch(E e, Q q);


    /**
     * 执行<i>DELETE FORM [TABLE] WHERE ...</i>
     * <ol>
     * <li><b>会</b>清空全部缓存</li>
     * <li><b>不会</b>按id清理缓存</li>
     * <li><b>不会</b>执行{@link win.doyto.query.entity.EntityAspect#afterDelete(Object)}</li>
     * </ol>
     *
     * @param q query object
     */
    int delete(Q q);

}
