package win.doyto.query.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import win.doyto.query.core.JoinQueryBuilder;
import win.doyto.query.core.PageQuery;
import win.doyto.query.core.SqlAndArgs;

import java.util.List;

/**
 * JoinQueryService
 *
 * @author f0rb on 2019-06-09
 */
@AllArgsConstructor
public class JoinQueryService<E, Q extends PageQuery> implements QueryService<E, Q> {

    @Autowired
    private JdbcOperations jdbcOperations;
    private final JoinQueryBuilder joinQueryBuilder;
    private final BeanPropertyRowMapper<E> beanPropertyRowMapper;

    public JoinQueryService(Class<E> entityClass) {
        this.joinQueryBuilder = new JoinQueryBuilder(entityClass);
        this.beanPropertyRowMapper = new BeanPropertyRowMapper<>(entityClass);
    }

    public JoinQueryService(JdbcOperations jdbcOperations, Class<E> entityClass) {
        this(entityClass);
        this.jdbcOperations = jdbcOperations;
    }

    public List<E> query(Q q) {
        SqlAndArgs sqlAndArgs = buildJoinSelectAndArgs(q);
        return jdbcOperations.query(sqlAndArgs.getSql(), beanPropertyRowMapper, sqlAndArgs.getArgs());
    }

    public long count(Q q) {
        SqlAndArgs sqlAndArgs = joinQueryBuilder.buildJoinCountAndArgs(q);
        return jdbcOperations.queryForObject(sqlAndArgs.getSql(), sqlAndArgs.getArgs(), Long.class);
    }

    public SqlAndArgs buildJoinSelectAndArgs(Q q) {
        return joinQueryBuilder.buildJoinSelectAndArgs(q);
    }
}
