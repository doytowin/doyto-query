package win.doyto.query.service;

import win.doyto.query.entity.Persistable;

/**
 * CommonCrudService
 *
 * @author f0rb on 2019-06-01
 */
interface CommonCrudService<E extends Persistable, I, Q> extends QueryService<E, I, Q> {

    void create(E e);

    int update(E e);

    default E save(E e) {
        if (e.isNew()) {
            create(e);
        } else {
            update(e);
        }
        return e;
    }

    int patch(E e);

    /**
     * 执行<i>INSERT INTO [TABLE] (col1, col2) VALUES (?), (?)</i>
     * <ol>
     * <li><b>会</b>清空全部缓存</li>
     * <li><b>不会</b>按id清理缓存</li>
     * <li><b>不会</b>执行{@link win.doyto.query.entity.EntityAspect#afterCreate(Object)}</li>
     * </ol>
     *
     * @param entities entities to insert
     * @return amount of updated entities
     */
    int batchInsert(Iterable<E> entities);

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
     * @return amount of updated entities
     */
    int patch(E e, Q q);


    /**
     * 执行<i>DELETE FORM [TABLE] WHERE ...</i>
     * <ol>
     * <li><b>会</b>清空全部缓存</li>
     * <li><b>不会</b>按id清理缓存</li>
     * <li><b>不会</b>执行{@link win.doyto.query.entity.EntityAspect#afterDelete(Object)}</li>
     * </ol>
     *
     * @param q query object
     * @return amount of updated entities
     */
    int delete(Q q);

}