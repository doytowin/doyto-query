package win.doyto.query.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.entity.UserIdProvider;
import win.doyto.query.service.AssociativeService;
import win.doyto.query.sql.AssociativeSqlBuilder;
import win.doyto.query.sql.SqlAndArgs;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.singleton;


/**
 * AssociativeServiceTemplate
 *
 * @author f0rb on 2019-05-30
 */
public class TemplateAssociativeService<L, R> implements AssociativeService<L, R> {

    private final AssociativeSqlBuilder sqlBuilder;
    private final SingleColumnRowMapper<L> leftRowMapper = new SingleColumnRowMapper<>();
    private final SingleColumnRowMapper<R> rightRowMapper = new SingleColumnRowMapper<>();

    private DatabaseOperations databaseOperations;

    @Autowired(required = false)
    private UserIdProvider<?> userIdProvider = () -> null;

    public TemplateAssociativeService(String table, String left, String right) {
        this(table, left, right, null);
    }

    public TemplateAssociativeService(String table, String left, String right, String createUserColumn) {
        this.sqlBuilder = new AssociativeSqlBuilder(table, left, right, createUserColumn);
    }

    @Autowired
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.databaseOperations = new DatabaseTemplate(jdbcOperations);
    }

    @Override
    public boolean exists(Collection<L> leftIds, Collection<R> rightIds) {
        return count(leftIds, rightIds) > 0;
    }

    @Override
    public long count(Collection<L> leftIds, Collection<R> rightIds) {
        return count(leftIds.toArray(), rightIds.toArray());
    }

    private Long count(Object[] leftIds, Object[] rightIds) {
        if (leftIds.length == 0 || rightIds.length == 0) {
            return 0L;
        }
        SqlAndArgs sqlAndArgs = sqlBuilder.buildCount(leftIds, rightIds);
        return databaseOperations.count(sqlAndArgs);
    }

    @Override
    public List<R> getByLeftId(L leftId) {
        SqlAndArgs sqlAndArgs = new SqlAndArgs(sqlBuilder.getGetByLeftId(), leftId);
        return databaseOperations.query(sqlAndArgs, rightRowMapper);
    }

    @Override
    public int deleteByLeftId(L leftId) {
        return databaseOperations.update(sqlBuilder.getDeleteByLeftId(), leftId);
    }

    @Override
    public List<L> getByRightId(R rightId) {
        SqlAndArgs sqlAndArgs = new SqlAndArgs(sqlBuilder.getGetByRightId(), rightId);
        return databaseOperations.query(sqlAndArgs, leftRowMapper);
    }

    @Override
    public int deleteByRightId(R rightId) {
        return databaseOperations.update(sqlBuilder.getDeleteByRightId(), rightId);
    }

    @Override
    public int deallocate(Collection<L> leftIds, Collection<R> rightIds) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildDeallocate(leftIds.toArray(), rightIds.toArray());
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    @Transactional
    public int reallocateForLeft(L leftId, Collection<R> rightIds) {
        deleteByLeftId(leftId);
        if (rightIds.isEmpty()) {
            return 0;
        }
        return allocate(singleton(leftId), rightIds);
    }

    @Override
    @Transactional
    public int reallocateForRight(R rightId, Collection<L> leftIds) {
        deleteByRightId(rightId);
        if (leftIds.isEmpty()) {
            return 0;
        }
        return allocate(leftIds, singleton(rightId));
    }

    @Override
    public int allocate(Collection<L> leftIds, Collection<R> rightIds) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildAllocate(leftIds, rightIds, userIdProvider.getUserId());
        return databaseOperations.update(sqlAndArgs);
    }

}
