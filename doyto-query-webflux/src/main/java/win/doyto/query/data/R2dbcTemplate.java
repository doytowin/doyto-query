package win.doyto.query.data;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.SqlAndArgs;

/**
 * R2dbcTemplate
 *
 * @author f0rb on 2021-11-20
 */
public class R2dbcTemplate implements R2dbcOperations {

    @Override
    public <V> Flux<V> query(SqlAndArgs sqlAndArgs, RowMapper<V> rowMapper) {
        return null;
    }

    @Override
    public Mono<Long> count(SqlAndArgs sqlAndArgs) {
        return Mono.just(3L);
    }

    @Override
    public Mono<Number> insert(SqlAndArgs sqlAndArgs) {
        return null;
    }

    @Override
    public Mono<Integer> update(SqlAndArgs sqlAndArgs) {
        return null;
    }
}
