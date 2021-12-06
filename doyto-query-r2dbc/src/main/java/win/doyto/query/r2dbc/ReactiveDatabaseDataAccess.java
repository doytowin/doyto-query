package win.doyto.query.r2dbc;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.Pageable;
import win.doyto.query.entity.Persistable;
import win.doyto.query.sql.SqlAndArgs;
import win.doyto.query.sql.SqlBuilder;
import win.doyto.query.sql.SqlBuilderFactory;
import win.doyto.query.util.ColumnUtil;

import java.io.Serializable;

/**
 * ReactiveDefaultDataAccess
 *
 * @author f0rb on 2021-11-18
 */
public class ReactiveDatabaseDataAccess<E extends Persistable<I>, I extends Serializable, Q extends Pageable> implements ReactiveDataAccess<E, I, Q> {

    private R2dbcOperations r2dbcOperations;
    private SqlBuilder<E> sqlBuilder;
    private RowMapper<E> rowMapper;
    private String[] selectColumns;

    public ReactiveDatabaseDataAccess(R2dbcOperations r2dbcOperations, Class<E> entityClass) {
        this.r2dbcOperations = r2dbcOperations;
        this.sqlBuilder = SqlBuilderFactory.create(entityClass);
        this.rowMapper = new BeanPropertyRowMapper<>(entityClass);
        this.selectColumns = ColumnUtil.resolveSelectColumns(entityClass);
    }

    @Override
    public Mono<E> create(E e) {
        return null;
    }

    @Override
    public Flux<E> query(Q q) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildSelectColumnsAndArgs(q, selectColumns);
        return r2dbcOperations.query(sqlAndArgs, rowMapper);
    }

    @Override
    public Mono<Long> count(Q q) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildCountAndArgs(q);
        return r2dbcOperations.count(sqlAndArgs);
    }

    @Override
    public Mono<E> get(I id) {
        return null;
    }

    @Override
    public Mono<Integer> delete(I id) {
        return null;
    }

    @Override
    public Mono<Integer> update(E e) {
        return null;
    }

    @Override
    public Mono<Integer> patch(E e) {
        return null;
    }
}
