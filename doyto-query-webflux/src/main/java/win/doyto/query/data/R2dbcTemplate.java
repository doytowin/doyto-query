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
import java.util.stream.Collectors;

/**
 * R2dbcTemplate
 *
 * @author f0rb on 2021-11-20
 */
@RequiredArgsConstructor
public class R2dbcTemplate implements R2dbcOperations {

    private final ConnectionFactory connectionFactory;

    private Function<Connection, Publisher<? extends Result>> createSqlExecutor(SqlAndArgs sqlAndArgs, String... idColumns) {
        return connection -> {
            Statement statement = connection.createStatement(sqlAndArgs.getSql());
            Object[] args = sqlAndArgs.getArgs();
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    statement.bind(i, args[i]);
                } else {
                    statement.bindNull(i, String.class);
                }
            }
            if (idColumns.length > 0) {
                statement = statement.returnGeneratedValues(idColumns);
            }
            return statement.execute();
        };
    }

    @Override
    public <V> Flux<V> query(SqlAndArgs sqlAndArgs, RowMapper<V> rowMapper) {
        return Mono.from(connectionFactory.create())
                   .flatMapMany(createSqlExecutor(sqlAndArgs))
                   .flatMap(result -> result.map(rowMapper));
    }

    @Override
    public Mono<Long> count(SqlAndArgs sqlAndArgs) {
        return Mono.from(connectionFactory.create())
                   .flatMapMany(createSqlExecutor(sqlAndArgs))
                   .flatMap(result -> result.map((row, md) -> row.get(0, Long.class)))
                   .single();
    }

    @Override
    public <I> Mono<I> insert(SqlAndArgs sqlAndArgs, String idColumn, Class<I> idClass) {
        return Mono.from(connectionFactory.create())
                   .flatMapMany(createSqlExecutor(sqlAndArgs, idColumn))
                   .flatMap(result -> result.map((row, md) -> row.get(idColumn, idClass)))
                   .single();
    }

    @Override
    public Mono<Integer> update(SqlAndArgs sqlAndArgs) {
        return Mono.from(connectionFactory.create())
                   .flatMapMany(createSqlExecutor(sqlAndArgs))
                   .flatMap(Result::getRowsUpdated)
                   .collect(Collectors.summingInt(Integer::intValue));
    }
}
