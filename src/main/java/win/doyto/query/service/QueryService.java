package win.doyto.query.service;

import win.doyto.query.core.PageQuery;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * QueryService
 *
 * @author f0rb
 */
public interface QueryService<E, Q extends PageQuery> {

    List<E> query(Q query);

    long count(Q query);

    default boolean exists(Q query) {
        return count(query) > 0;
    }

    default boolean notExists(Q query) {
        return !exists(query);
    }

    default E get(Q query) {
        return CollectionUtil.first(query(query), query);
    }

    default <V> V get(Q query, Function<E, V> transfer) {
        return CollectionUtil.first(query(query, transfer), query);
    }

    default <V> List<V> query(Q query, Function<E, V> transfer) {
        return query(query).stream().map(transfer).collect(Collectors.toList());
    }

    default PageList<E> page(Q query) {
        query.forcePaging();
        return new PageList<>(query(query), count(query));
    }

    default <V> PageList<V> page(Q query, Function<E, V> transfer) {
        query.forcePaging();
        return new PageList<>(query(query, transfer), count(query));
    }

}
