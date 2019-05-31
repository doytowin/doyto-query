package win.doyto.query.service;

import org.springframework.jdbc.core.RowMapper;

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

    default <V> List<V> query(Q query, Function<E, V> transfer) {
        return query(query).stream().map(transfer).collect(Collectors.toList());
    }

    default <V> PageList<V> page(Q query, Function<E, V> transfer) {
        return new PageList<>(query(query, transfer), count(query));
    }

    <V> List<V> queryColumns(Q q, RowMapper<V> rowMapper, String... columns);

    default <V> V getColumns(Q query, RowMapper<V> rowMapper, String... columns) {
        return CollectionUtil.first(queryColumns(query, rowMapper, columns));
    }

}
