package win.doyto.query.jdbc;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import win.doyto.query.core.DataQuery;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.sql.JoinQueryBuilder;
import win.doyto.query.sql.SqlAndArgs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JoinQueryService
 *
 * @author f0rb on 2019-06-09
 */
public class JdbcComplexDataQuery implements DataQuery {

    private Map<Class<?>, RowMapper<?>> holder = new HashMap<>();
    private DatabaseOperations databaseOperations;

    public JdbcComplexDataQuery(JdbcOperations jdbcOperations) {
        this.databaseOperations = new DatabaseTemplate(jdbcOperations);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, Q extends DoytoQuery> List<E> query(Q query, Class<E> entityClass) {
        RowMapper<E> rowMapper = (RowMapper<E>) holder.computeIfAbsent(entityClass, BeanPropertyRowMapper::new);
        SqlAndArgs sqlAndArgs = JoinQueryBuilder.buildSelectAndArgs(query, entityClass);
        return databaseOperations.query(sqlAndArgs, rowMapper);
    }

    @Override
    public <E, Q extends DoytoQuery> Long count(Q query, Class<E> entityClass) {
        SqlAndArgs sqlAndArgs = JoinQueryBuilder.buildCountAndArgs(query, entityClass);
        return databaseOperations.count(sqlAndArgs);
    }

}
