package win.doyto.query.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import win.doyto.query.sql.SqlAndArgs;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/**
 * DbTemplate
 *
 * @author f0rb on 2021-08-30
 */
@RequiredArgsConstructor
public class DatabaseTemplate implements DatabaseOperations {

    private final JdbcOperations jdbcOperations;
    private final RowMapper<Long> countRowMap = new SingleColumnRowMapper<>(Long.class);

    @Override
    public <V> List<V> query(SqlAndArgs sqlAndArgs, RowMapper<V> rowMapper) {
        return jdbcOperations.query(sqlAndArgs.getSql(), rowMapper, sqlAndArgs.getArgs());
    }

    @Override
    public int update(SqlAndArgs sqlAndArgs) {
        return jdbcOperations.update(sqlAndArgs.getSql(), sqlAndArgs.getArgs());
    }

    @Override
    public long count(SqlAndArgs sqlAndArgs) {
        return jdbcOperations.queryForObject(sqlAndArgs.getSql(), countRowMap, sqlAndArgs.getArgs());
    }

    public Number insert(SqlAndArgs sqlAndArgs) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcOperations.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlAndArgs.getSql(), Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            for (Object arg : sqlAndArgs.getArgs()) {
                ps.setObject(i++, arg);
            }
            return ps;
        }, keyHolder);
        return keyHolder.getKey();
    }
}
