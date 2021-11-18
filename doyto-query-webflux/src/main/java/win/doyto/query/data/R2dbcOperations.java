package win.doyto.query.data;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.SqlAndArgs;

/**
 * ReactiveDatabaseOptions
 *
 * @author f0rb on 2021-11-18
 */
public interface R2dbcOperations {

    <V> Flux<V> query(SqlAndArgs sqlAndArgs, RowMapper<V> rowMapper);

    Mono<Long> count(SqlAndArgs sqlAndArgs);

    Mono<Number> insert(SqlAndArgs sqlAndArgs);

    Mono<Integer> update(SqlAndArgs sqlAndArgs);

    default Mono<Integer> update(String sql, Object... args) {
        return update(new SqlAndArgs(sql, args));
    }

}

