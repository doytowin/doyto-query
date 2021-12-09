package win.doyto.query.service;

import win.doyto.query.core.DoytoQuery;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * QueryService
 *
 * @author f0rb
 */
public interface QueryService<E, Q extends DoytoQuery> {

    List<E> query(Q query);

    long count(Q query);

    default boolean exists(Q query) {
        return count(query) > 0;
    }

    default boolean notExists(Q query) {
        return !exists(query);
    }

    default E get(Q query) {
        return get(query, e -> e);
    }

    default <V> V get(Q query, Function<E, V> transfer) {
        query.setPageSize(1);
        List<E> list = query(query);
        return list.isEmpty() ? null : transfer.apply(list.get(0));
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
