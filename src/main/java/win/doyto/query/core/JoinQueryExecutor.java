package win.doyto.query.core;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;

import java.util.List;

/**
 * JoinQueryExecutor
 *
 * @author f0rb on 2019-06-09
 */
@AllArgsConstructor
public class JoinQueryExecutor<E, Q extends PageQuery> {

    private final JdbcOperations jdbcOperations;
    private final JoinQueryBuilder joinQueryBuilder;
    private BeanPropertyRowMapper<E> tBeanPropertyRowMapper;

    public JoinQueryExecutor(JdbcOperations jdbcOperations, Class<E> entityClass) {
        this.jdbcOperations = jdbcOperations;
        this.joinQueryBuilder = new JoinQueryBuilder(entityClass);
        this.tBeanPropertyRowMapper = new BeanPropertyRowMapper<>(entityClass);
    }

    public List<E> execute(Q q) {
        SqlAndArgs sqlAndArgs = buildJoinSelectAndArgs(q);
        return jdbcOperations.query(sqlAndArgs.getSql(), tBeanPropertyRowMapper, sqlAndArgs.getArgs());
    }

    public SqlAndArgs buildJoinSelectAndArgs(Q q) {
        return joinQueryBuilder.buildJoinSelectAndArgs(q);
    }
}
