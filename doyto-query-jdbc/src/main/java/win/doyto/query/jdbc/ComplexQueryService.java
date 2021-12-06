package win.doyto.query.jdbc;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import win.doyto.query.core.Pageable;
import win.doyto.query.service.QueryService;
import win.doyto.query.sql.JoinQueryBuilder;
import win.doyto.query.sql.SqlAndArgs;

import java.util.List;

/**
 * JoinQueryService
 *
 * @author f0rb on 2019-06-09
 */
@AllArgsConstructor
public class ComplexQueryService<E, Q extends Pageable> implements QueryService<E, Q> {

    private DatabaseOperations databaseOperations;
    private final JoinQueryBuilder joinQueryBuilder;
    private final BeanPropertyRowMapper<E> beanPropertyRowMapper;

    public ComplexQueryService(Class<E> entityClass) {
        this.joinQueryBuilder = new JoinQueryBuilder(entityClass);
        this.beanPropertyRowMapper = new BeanPropertyRowMapper<>(entityClass);
    }

    @Autowired
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.databaseOperations = new DatabaseTemplate(jdbcOperations);
    }

    public List<E> query(Q q) {
        SqlAndArgs sqlAndArgs = buildJoinSelectAndArgs(q);
        return databaseOperations.query(sqlAndArgs, beanPropertyRowMapper);
    }

    public long count(Q q) {
        SqlAndArgs sqlAndArgs = joinQueryBuilder.buildJoinCountAndArgs(q);
        return databaseOperations.count(sqlAndArgs);
    }

    public SqlAndArgs buildJoinSelectAndArgs(Q q) {
        return joinQueryBuilder.buildJoinSelectAndArgs(q);
    }

}
