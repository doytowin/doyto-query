package win.doyto.query.data;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.SqlAndArgs;

/**
 * R2dbcTemplate
 *
 * @author f0rb on 2021-11-20
 */
@RequiredArgsConstructor
public class R2dbcTemplate implements R2dbcOperations {

    private final ConnectionFactory connectionFactory;

    @Override
    public <V> Flux<V> query(SqlAndArgs sqlAndArgs, RowMapper<V> rowMapper) {
        return null;
    }

    @Override
    public Mono<Long> count(SqlAndArgs sqlAndArgs) {
        return Mono.from(connectionFactory.create())
                   .flatMapMany(
                           connection -> {
                               Statement statement = connection.createStatement(sqlAndArgs.getSql());
                               return statement.execute();
                           })
                   .flatMap(result -> result.map((row, rowMetadata) -> row.get(0, Long.class)))
                   .singleOrEmpty();
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
