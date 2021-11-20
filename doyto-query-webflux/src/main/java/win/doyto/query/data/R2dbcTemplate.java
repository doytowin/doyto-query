package win.doyto.query.data;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.SqlAndArgs;

import java.util.function.Function;

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
                               Object[] args = sqlAndArgs.getArgs();
                               for (int i = 0; i < args.length; i++) {
                                   statement.bind(i, args[i]);
                               }
                               return statement.execute();
                           })
                   .flatMap(result -> result.map((row, rowMetadata) -> row.get(0, Long.class)))
                   .singleOrEmpty();
    }

    @Override
    public <I> Mono<I> insert(SqlAndArgs sqlAndArgs, String idColumn, Class<I> idClass) {
        Function<Connection, Publisher<? extends Result>> mapper =
                connection -> {
                    Statement statement = connection.createStatement(sqlAndArgs.getSql());
                    Object[] args = sqlAndArgs.getArgs();
                    for (int i = 0; i < args.length; i++) {
                        statement.bind(i, args[i]);
                    }
                    return statement.returnGeneratedValues(idColumn).execute();
                };
        return Mono.from(connectionFactory.create())
                   .flatMapMany(mapper)
                   .flatMap(result -> result.map((row, md) -> row.get(idColumn, idClass)))
                   .single();
    }

    @Override
    public Mono<Integer> update(SqlAndArgs sqlAndArgs) {
        return null;
    }
}
