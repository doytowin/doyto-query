package win.doyto.query.data;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import win.doyto.query.core.*;
import win.doyto.query.entity.Persistable;
import win.doyto.query.util.ColumnUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * JdbcDataAccess
 *
 * @author f0rb
 */
public final class DefaultDataAccess<E extends Persistable<I>, I extends Serializable, Q extends PageQuery> implements DataAccess<E, I, Q> {

    private static final Map<Class<?>, RowMapper<?>> classRowMapperMap;

    static {
        classRowMapperMap = new ConcurrentHashMap<>();
        classRowMapperMap.put(Map.class, new ColumnMapRowMapper());
    }

    private final DatabaseOperations databaseOperations;
    private final RowMapper<E> rowMapper;
    private final SqlBuilder<E> sqlBuilder;
    private final String[] columnsForSelect;
    private final boolean isGeneratedId;
    private final BiConsumer<E, Number> setIdFunc;

    @SuppressWarnings("unchecked")
    public DefaultDataAccess(DatabaseOperations databaseOperations, Class<E> entityClass, Class<I> idClass, RowMapper<E> rowMapper) {
        classRowMapperMap.put(entityClass, rowMapper);
        this.databaseOperations = databaseOperations;
        this.rowMapper = rowMapper;
        this.sqlBuilder = SqlBuilderFactory.create(entityClass);
        this.columnsForSelect = ColumnUtil.resolveSelectColumns(entityClass);

        Field[] idFields = FieldUtils.getFieldsWithAnnotation(entityClass, Id.class);
        this.isGeneratedId = idFields.length == 1 && idFields[0].isAnnotationPresent(GeneratedValue.class);

        if (idClass.isAssignableFrom(Integer.class)) {
            setIdFunc = (e, key) -> e.setId((I) (Integer) key.intValue());
        } else if (idClass.isAssignableFrom(Long.class)) {
            setIdFunc = (e, key) -> e.setId((I) (Long) key.longValue());
        } else {
            setIdFunc = (e, key) -> e.setId((I) key);
        }
    }

    @Override
    public final List<E> query(Q q) {
        return queryColumns(q, rowMapper, columnsForSelect);
    }

    @Override
    public final <V> List<V> queryColumns(Q q, Class<V> clazz, String... columns) {
        @SuppressWarnings("unchecked")
        RowMapper<V> localRowMapper = (RowMapper<V>) classRowMapperMap.computeIfAbsent(
                clazz, c -> ColumnUtil.isSingleColumn(columns) ? new SingleColumnRowMapper<>(clazz) : new BeanPropertyRowMapper<>(clazz));
        return queryColumns(q, localRowMapper, columns);
    }

    private <V> List<V> queryColumns(Q q, RowMapper<V> rowMapper, String... columns) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildSelectColumnsAndArgs(q, columns);
        return databaseOperations.query(sqlAndArgs, rowMapper);
    }

    @Override
    public final long count(Q q) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildCountAndArgs(q);
        return databaseOperations.count(sqlAndArgs);
    }

    @Override
    public final int delete(Q q) {
        return databaseOperations.update(sqlBuilder.buildDeleteAndArgs(q));
    }

    @Override
    public final E get(IdWrapper<I> w) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildSelectById(w, columnsForSelect);
        List<E> list = databaseOperations.query(sqlAndArgs, rowMapper);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public int delete(IdWrapper<I> w) {
        return databaseOperations.update(sqlBuilder.buildDeleteById(w));
    }

    @Override
    public final void create(E e) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildCreateAndArgs(e);

        if (isGeneratedId) {
            Number key = databaseOperations.insert(sqlAndArgs);
            setIdFunc.accept(e, key);
        } else {
            databaseOperations.update(sqlAndArgs);
        }
    }

    @Override
    public int batchInsert(Iterable<E> entities, String... columns) {
        if (!entities.iterator().hasNext()) {
            return 0;
        }
        return databaseOperations.update(sqlBuilder.buildCreateAndArgs(entities, columns));
    }

    @Override
    public final int update(E e) {
        return databaseOperations.update(sqlBuilder.buildUpdateAndArgs(e));
    }

    @Override
    public final int patch(E e) {
        return databaseOperations.update(sqlBuilder.buildPatchAndArgsWithId(e));
    }

    @Override
    public final int patch(E e, Q q) {
        return databaseOperations.update(sqlBuilder.buildPatchAndArgsWithQuery(e, q));
    }

    @Override
    public List<I> queryIds(Q query) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildSelectIdAndArgs(query);
        return databaseOperations.query(sqlAndArgs, new SingleColumnRowMapper<>());
    }

}
