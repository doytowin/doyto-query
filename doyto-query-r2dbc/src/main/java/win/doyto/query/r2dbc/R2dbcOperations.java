package win.doyto.query.r2dbc;

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

    <I> Mono<I> insert(SqlAndArgs sqlAndArgs, String idColumn, Class<I> idClass);

    Mono<Integer> update(SqlAndArgs sqlAndArgs);

    default Mono<Integer> update(String sql, Object... args) {
        return update(new SqlAndArgs(sql, args));
    }

}

