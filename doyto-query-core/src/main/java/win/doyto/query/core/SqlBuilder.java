package win.doyto.query.core;

import win.doyto.query.entity.Persistable;

/**
 * SqlBuilder
 *
 * @author f0rb on 2021-11-21
 */
public interface SqlBuilder<E extends Persistable<?>> {

    SqlAndArgs buildCountAndArgs(Pageable query);

    SqlAndArgs buildSelectColumnsAndArgs(Pageable query, String... columns);

    SqlAndArgs buildSelectById(IdWrapper<?> idWrapper, String... columns);

    SqlAndArgs buildSelectIdAndArgs(Pageable query);

    SqlAndArgs buildCreateAndArgs(E testEntity);

    SqlAndArgs buildCreateAndArgs(Iterable<E> entities, String... columns);

    SqlAndArgs buildUpdateAndArgs(E entity);

    SqlAndArgs buildPatchAndArgsWithId(E entity);

    SqlAndArgs buildPatchAndArgsWithQuery(E entity, Pageable query);

    SqlAndArgs buildDeleteAndArgs(Pageable query);

    SqlAndArgs buildDeleteById(IdWrapper<?> w);

}
