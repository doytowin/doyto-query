package win.doyto.query.service;

import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.util.List;

/**
 * DynamicService
 *
 * @author f0rb on 2019-06-01
 */
public interface DynamicService<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery> extends QueryService<E, Q> {

    List<I> queryIds(Q query);

    <V> List<V> queryColumns(Q query, Class<V> clazz, String... columns);

    void create(E e);

    int update(E e);

    default E save(E e) {
        if (isNewEntity(e)) {
            create(e);
        } else {
            update(e);
        }
        return e;
    }

    default boolean isNewEntity(E e) {
        return e.isNew();
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
     * @param columns update columns on duplicate
     * @return amount of updated entities
     */
    int create(Iterable<E> entities, String... columns);

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

    /**
     * Get origin entity from sharding table
     *
     * @param w an entity just contains id and information of sharding table
     * @return origin entity
     */
    E get(IdWrapper<I> w);

    /**
     * force to get a new entity object from database
     *
     * @param w entity id
     * @return a new entity object
     */
    E fetch(IdWrapper<I> w);

    /**
     * Delete entity from sharding table
     *
     * @param w an entity just contains id and information of sharding table
     * @return origin entity
     */
    E delete(IdWrapper<I> w);

}
