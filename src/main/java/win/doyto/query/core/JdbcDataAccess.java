package win.doyto.query.core;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JdbcDataAccess
 *
 * @author f0rb
 */
final class JdbcDataAccess<E extends Persistable<I>, I extends Serializable, Q> implements DataAccess<E, I, Q> {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<E> rowMapper;
    private final CrudBuilder<E> crudBuilder;

    public JdbcDataAccess(JdbcTemplate jdbcTemplate, Class<E> entityClass) {
        this.jdbcTemplate = jdbcTemplate;
        rowMapper = new BeanPropertyRowMapper<>(entityClass);
        crudBuilder = new CrudBuilder<>(entityClass);
    }

    @Override
    public final List<E> query(Q q) {
        return queryColumns(q, rowMapper, "*");
    }

    @Override
    public final <V> List<V> queryColumns(Q q, RowMapper<V> rowMapper, String... columns) {
        SqlAndArgs sqlAndArgs = crudBuilder.buildSelectColumnsAndArgs(q, columns);
        return jdbcTemplate.query(sqlAndArgs.sql, sqlAndArgs.args, rowMapper);
    }

    @Override
    public final long count(Q q) {
        SqlAndArgs sqlAndArgs = crudBuilder.buildCountAndArgs(q);
        return jdbcTemplate.queryForObject(sqlAndArgs.sql, sqlAndArgs.args, Long.class);
    }

    @Override
    public final int delete(Q q) {
        return doUpdate(crudBuilder.buildDeleteAndArgs(q));
    }

    @Override
    public final E get(I id) {
        return getEntity(crudBuilder.buildSelectById(), id);
    }

    @Override
    public final int delete(I id) {
        return jdbcTemplate.update(crudBuilder.buildDeleteById(), id);
    }

    @Override
    public final E get(E e) {
        return getEntity(crudBuilder.buildSelectById(e), e.getId());
    }

    private E getEntity(String sql, I id) {
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, id);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    @Override
    public final int delete(E e) {
        return jdbcTemplate.update(crudBuilder.buildDeleteById(e), e.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void create(E e) {
        List<Object> args = new ArrayList<>();
        String sql = crudBuilder.buildCreateAndArgs(e, args);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            for (Object arg : args) {
                ps.setObject(i++, arg);
            }
            return ps;
        }, keyHolder);
        e.setId((I) keyHolder.getKey());
    }

    private int doUpdate(SqlAndArgs sql) {
        return jdbcTemplate.update(sql.sql, sql.args);
    }

    @Override
    public final void update(E e) {
        doUpdate(crudBuilder.buildUpdateAndArgs(e));
    }

    @Override
    public final void patch(E e) {
        doUpdate(crudBuilder.buildPatchAndArgsWithId(e));
    }

    @Override
    public final void patch(E e, Q q) {
        doUpdate(crudBuilder.buildPatchAndArgsWithQuery(e, q));
    }

    @Override
    public final E fetch(I id) {
        return get(id);
    }

}
