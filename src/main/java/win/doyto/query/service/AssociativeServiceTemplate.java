package win.doyto.query.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.core.SqlAndArgs;
import win.doyto.query.entity.UserIdProvider;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.singleton;


/**
 * AssociativeServiceTemplate
 *
 * @author f0rb on 2019-05-30
 */
public class AssociativeServiceTemplate<L, R> implements AssociativeService<L, R> {

    private final AssociativeSqlBuilder sqlBuilder;
    private final SingleColumnRowMapper<L> leftRowMapper = new SingleColumnRowMapper<>();
    private final SingleColumnRowMapper<R> rightRowMapper = new SingleColumnRowMapper<>();

    @Autowired
    private JdbcOperations jdbcOperations;

    @Autowired(required = false)
    private UserIdProvider userIdProvider;

    public AssociativeServiceTemplate(String table, String left, String right) {
        this(table, left, right, null);
    }

    public AssociativeServiceTemplate(String table, String left, String right, String createUserColumn) {
        this.sqlBuilder = new AssociativeSqlBuilder(table, left, right, createUserColumn);
    }

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
        return jdbcOperations.queryForObject(sqlAndArgs.getSql(), sqlAndArgs.getArgs(), Long.class);
    }

    @Override
    public List<R> getByLeftId(L leftId) {
        return jdbcOperations.query(sqlBuilder.getByLeftId, rightRowMapper, leftId);
    }

    @Override
    public void deleteByLeftId(L leftId) {
        jdbcOperations.update(sqlBuilder.deleteByLeftId, leftId);
    }

    @Override
    public List<L> getByRightId(R rightId) {
        return jdbcOperations.query(sqlBuilder.getByRightId, leftRowMapper, rightId);
    }

    @Override
    public void deleteByRightId(R rightId) {
        jdbcOperations.update(sqlBuilder.deleteByRightId, rightId);
    }

    private void deallocate(Object[] leftIds, Object[] rightIds) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildDeallocate(leftIds, rightIds);
        jdbcOperations.update(sqlAndArgs.getSql(), sqlAndArgs.getArgs());
    }

    @Override
    public void deallocate(Collection<L> leftIds, Collection<R> rightIds) {
        deallocate(leftIds.toArray(), rightIds.toArray());
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
    public int allocate(L leftId, R rightId) {
        return !exists(leftId, rightId) ? allocate(singleton(leftId), singleton(rightId)) : 0;
    }

    public int allocate(Collection<L> leftIds, Collection<R> rightIds) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildAllocate(
            leftIds, rightIds, (Long) (userIdProvider == null ? null : userIdProvider.getUserId()));
        return jdbcOperations.update(sqlAndArgs.getSql(), sqlAndArgs.getArgs());
    }

}
