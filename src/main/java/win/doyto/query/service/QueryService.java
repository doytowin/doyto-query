package win.doyto.query.service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * QueryService
 *
 * @author f0rb
 */
public interface QueryService<E, I, Q> {

    List<E> query(Q query);

    List<I> queryIds(Q query);

    long count(Q query);

    <V> List<V> queryColumns(Q query, Class<V> clazz, String... columns);

    default boolean exists(Q query) {
        return count(query) > 0;
    }

    default boolean notExists(Q query) {
        return !exists(query);
    }

    default E get(Q query) {
        return CollectionUtil.first(query(query));
    }

    default <V> V get(Q query, Function<E, V> transfer) {
        return CollectionUtil.first(query(query, transfer));
    }

    default <V> List<V> query(Q query, Function<E, V> transfer) {
        return query(query).stream().map(transfer).collect(Collectors.toList());
    }

    default <V> PageList<V> page(Q query, Function<E, V> transfer) {
        return new PageList<>(query(query, transfer), count(query));
    }

}
