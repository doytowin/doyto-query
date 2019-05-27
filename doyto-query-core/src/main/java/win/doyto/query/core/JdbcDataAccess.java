package win.doyto.query.core;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.Assert;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Table;

import static win.doyto.query.core.CrudBuilder.replaceTableName;

/**
 * JdbcDataAccess
 *
 * @author f0rb
 */
class JdbcDataAccess<E extends Persistable<I>, I extends Serializable, Q> implements DataAccess<E, I, Q> {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<E> rowMapper;
    private final String getById;
    private final String deleteById;
    private final CrudBuilder<E> crudBuilder;
    private static final String FMT_GET_BY_ID = "SELECT * FROM %s WHERE id = ?";
    private static final String FMT_DELETE_BY_ID = "DELETE FROM %s WHERE id = ?";
    private final String domainTable;

    public JdbcDataAccess(JdbcTemplate jdbcTemplate, Class<E> domainType) {
        this.jdbcTemplate = jdbcTemplate;

        rowMapper = new BeanPropertyRowMapper<>(domainType);

        domainTable = domainType.getAnnotation(Table.class).name();
        getById = String.format(FMT_GET_BY_ID, domainTable);
        deleteById = String.format(FMT_DELETE_BY_ID, domainTable);
        crudBuilder = new CrudBuilder<>(domainType);
    }

    @Override
    public List<E> query(Q q) {
        LinkedList<Object> args = new LinkedList<>();
        String sql = crudBuilder.buildSelectAndArgs(q, args);
        return jdbcTemplate.query(sql, args.toArray(), rowMapper);
    }

    @Override
    public long count(Q q) {
        LinkedList<Object> args = new LinkedList<>();
        String sql = crudBuilder.buildCountAndArgs(q, args);
        //noinspection ConstantConditions
        return jdbcTemplate.queryForObject(sql, args.toArray(), Long.class);
    }

    @Override
    public E get(I id) {
        Assert.notNull(id, "The given id must not be null!");
        return jdbcTemplate.queryForObject(getById, rowMapper, id);
    }

    @Override
    public int delete(I id) {
        Assert.notNull(id, "The given id must not be null!");
        return jdbcTemplate.update(deleteById, id);
    }

    @Override
    public E get(E e) {
        String sql = String.format(FMT_GET_BY_ID, replaceTableName(e, domainTable));
        return jdbcTemplate.queryForObject(sql, rowMapper, e.getId());
    }

    @Override
    public int delete(E e) {
        return jdbcTemplate.update(String.format(FMT_DELETE_BY_ID, replaceTableName(e, domainTable)), e.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void create(E e) {
        LinkedList<Object> args = new LinkedList<>();
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

    @Override
    public void update(E e) {
        LinkedList<Object> args = new LinkedList<>();
        String sql = crudBuilder.buildUpdateAndArgs(e, args);
        jdbcTemplate.update(sql, args.toArray());
    }

    @Override
    public void patch(E e) {
        LinkedList<Object> args = new LinkedList<>();
        String sql = crudBuilder.buildPatchAndArgs(e, args);
        jdbcTemplate.update(sql, args.toArray());
    }

    @Override
    public E fetch(I id) {
        return get(id);
    }

}
