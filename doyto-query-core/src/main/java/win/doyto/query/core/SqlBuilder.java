package win.doyto.query.core;

import win.doyto.query.entity.Persistable;

/**
 * SqlBuilder
 *
 * @author f0rb on 2021-11-21
 */
public interface SqlBuilder<E extends Persistable<?>> {

    SqlAndArgs buildCountAndArgs(PageQuery query);

    SqlAndArgs buildSelectColumnsAndArgs(PageQuery query, String... columns);

    SqlAndArgs buildSelectById(IdWrapper<?> idWrapper, String... columns);

    SqlAndArgs buildSelectIdAndArgs(PageQuery query);

    SqlAndArgs buildCreateAndArgs(E testEntity);

    SqlAndArgs buildCreateAndArgs(Iterable<E> entities, String... columns);

    SqlAndArgs buildUpdateAndArgs(E entity);

    SqlAndArgs buildPatchAndArgsWithId(E entity);

    SqlAndArgs buildPatchAndArgsWithQuery(E entity, PageQuery query);

    SqlAndArgs buildDeleteAndArgs(PageQuery query);

    SqlAndArgs buildDeleteById(IdWrapper<?> w);

}
