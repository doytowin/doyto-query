package win.doyto.query.jdbc;

import org.springframework.jdbc.core.RowMapper;
import win.doyto.query.sql.SqlAndArgs;

import java.util.List;

/**
 * DbOperations
 *
 * @author f0rb on 2021-08-29
 */
public interface DatabaseOperations {

    <V> List<V> query(SqlAndArgs sqlAndArgs, RowMapper<V> rowMapper);

    long count(SqlAndArgs sqlAndArgs);

    Number insert(SqlAndArgs sqlAndArgs);

    int update(SqlAndArgs sqlAndArgs);

    default int update(String sql, Object... args) {
        return update(new SqlAndArgs(sql, args));
    }
}
