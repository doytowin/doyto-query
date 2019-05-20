package win.doyto.query.core;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * QueryService
 *
 * @author f0rb
 */
public interface QueryService<E, Q> {

    List<E> query(Q query);

    long count(Q query);

    default E get(Q query) {
        return CollectionUtil.first(query(query));
    }

    default PageList<E> page(Q query) {
        return new PageList<>(query(query), count(query));
    }

    default <V> List<V> query(Q query, Function<E, V> transfer) {
        return query(query).stream().map(transfer).collect(Collectors.toList());
    }

    default <V> PageList<V> page(Q query, Function<E, V> transfer) {
        return new PageList<>(query(query, transfer), count(query));
    }

}
