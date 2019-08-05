package win.doyto.query.core;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * JdbcDataAccess
 *
 * @author f0rb
 */
public final class JdbcDataAccess<E extends Persistable<I>, I extends Serializable, Q extends PageQuery> implements DataAccess<E, I, Q> {

    private static final Map<Class, RowMapper> classRowMapperMap = new ConcurrentHashMap<>();
    private final JdbcOperations jdbcOperations;
    private final RowMapper<E> rowMapper;
    private final CrudBuilder<E> crudBuilder;
    private final String[] columnsForSelect;
    private final boolean isGeneratedId;
    private final BiFunction<E, Number, Void> setIdFunc;

    @SuppressWarnings("unchecked")
    public JdbcDataAccess(JdbcOperations jdbcOperations, Class<E> entityClass, Class<I> idClass, RowMapper<E> rowMapper) {
        this.jdbcOperations = jdbcOperations;
        this.rowMapper = rowMapper;
        crudBuilder = new CrudBuilder<>(entityClass);
        columnsForSelect = Arrays
            .stream(FieldUtils.getAllFields(entityClass))
            .filter(JdbcDataAccess::shouldRetain)
            .map(CommonUtil::selectAs)
            .toArray(String[]::new);

        Field[] idFields = FieldUtils.getFieldsWithAnnotation(entityClass, Id.class);
        isGeneratedId = idFields.length == 1 && idFields[0].isAnnotationPresent(GeneratedValue.class);

        if (idClass.isAssignableFrom(Integer.class)) {
            setIdFunc = (e, key) -> {
                e.setId((I) (Integer) key.intValue());
                return null;
            };
        } else if (idClass.isAssignableFrom(Long.class)) {
            setIdFunc = (e, key) -> {
                e.setId((I) (Long) key.longValue());
                return null;
            };
        } else {
            setIdFunc = (e, key) -> {
                e.setId((I) key);
                return null;
            };
        }
    }

    private static boolean shouldRetain(Field field) {
        return !field.getName().startsWith("$")              // $jacocoData
            && !Modifier.isStatic(field.getModifiers())      // static field
            && !field.isAnnotationPresent(Transient.class)   // Transient field
            ;
    }

    @Override
    public final List<E> query(Q q) {
        return queryColumns(q, rowMapper, columnsForSelect);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <V> List<V> queryColumns(Q q, Class<V> clazz, String... columns) {
        RowMapper customRowMapper = classRowMapperMap.computeIfAbsent(clazz, columns.length == 1 ? SingleColumnRowMapper::new : BeanPropertyRowMapper::new);
        return queryColumns(q, customRowMapper, columns);
    }

    private <V> List<V> queryColumns(Q q, RowMapper<V> rowMapper, String... columns) {
        SqlAndArgs sqlAndArgs = crudBuilder.buildSelectColumnsAndArgs(q, columns);
        return jdbcOperations.query(sqlAndArgs.sql, sqlAndArgs.args, rowMapper);
    }

    @Override
    public final long count(Q q) {
        SqlAndArgs sqlAndArgs = crudBuilder.buildCountAndArgs(q);
        return jdbcOperations.queryForObject(sqlAndArgs.sql, sqlAndArgs.args, Long.class);
    }

    @Override
    public final int delete(Q q) {
        return doUpdate(crudBuilder.buildDeleteAndArgs(q));
    }

    @Override
    public final E get(I id) {
        return getEntity(crudBuilder.buildSelectById(id, columnsForSelect));
    }

    @Override
    public final int delete(I id) {
        return jdbcOperations.update(crudBuilder.buildDeleteById(), id);
    }

    @Override
    public final E get(E e) {
        return getEntity(crudBuilder.buildSelectById(e, columnsForSelect));
    }

    private E getEntity(SqlAndArgs sqlAndArgs) {
        List<E> list = jdbcOperations.query(sqlAndArgs.sql, sqlAndArgs.args, rowMapper);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public final int delete(E e) {
        return jdbcOperations.update(crudBuilder.buildDeleteById(e), e.getId());
    }

    @Override
    public final void create(E e) {
        List<Object> args = new ArrayList<>();
        String sql = crudBuilder.buildCreateAndArgs(e, args);

        if (isGeneratedId) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcOperations.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                int i = 1;
                for (Object arg : args) {
                    ps.setObject(i++, arg);
                }
                return ps;
            }, keyHolder);
            setIdFunc.apply(e, keyHolder.getKey());
        } else {
            jdbcOperations.update(sql, args.toArray());
        }
    }

    @Override
    public int batchInsert(Iterable<E> entities, String... columns) {
        if (!entities.iterator().hasNext()) {
            return 0;
        }
        return doUpdate(crudBuilder.buildCreateAndArgs(entities, columns));
    }

    private int doUpdate(SqlAndArgs sqlAndArgs) {
        return jdbcOperations.update(sqlAndArgs.sql, sqlAndArgs.args);
    }

    @Override
    public final int update(E e) {
        return doUpdate(crudBuilder.buildUpdateAndArgs(e));
    }

    @Override
    public final int patch(E e) {
        return doUpdate(crudBuilder.buildPatchAndArgsWithId(e));
    }

    @Override
    public final int patch(E e, Q q) {
        return doUpdate(crudBuilder.buildPatchAndArgsWithQuery(e, q));
    }

    @Override
    public List<I> queryIds(Q query) {
        SqlAndArgs sqlAndArgs = crudBuilder.buildSelectIdAndArgs(query);
        return jdbcOperations.query(sqlAndArgs.sql, sqlAndArgs.args, new SingleColumnRowMapper<>());
    }

}
