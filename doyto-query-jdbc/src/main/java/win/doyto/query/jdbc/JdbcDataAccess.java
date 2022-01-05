/*
 * Copyright © 2019-2022 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.jdbc;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.jdbc.core.*;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.Persistable;
import win.doyto.query.sql.SqlAndArgs;
import win.doyto.query.sql.SqlBuilder;
import win.doyto.query.sql.SqlBuilderFactory;
import win.doyto.query.util.BeanUtil;
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
public final class JdbcDataAccess<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery> implements DataAccess<E, I, Q> {

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

    public JdbcDataAccess(JdbcOperations jdbcOperations, Class<E> entityClass) {
        this(new DatabaseTemplate(jdbcOperations), entityClass, new BeanPropertyRowMapper<>(entityClass));
    }

    @SuppressWarnings("unchecked")
    public JdbcDataAccess(DatabaseOperations databaseOperations, Class<E> entityClass, RowMapper<E> rowMapper) {
        classRowMapperMap.put(entityClass, rowMapper);
        this.databaseOperations = databaseOperations;
        this.rowMapper = rowMapper;
        this.sqlBuilder = SqlBuilderFactory.create(entityClass);
        this.columnsForSelect = ColumnUtil.resolveSelectColumns(entityClass);

        Field[] idFields = FieldUtils.getFieldsWithAnnotation(entityClass, Id.class);
        this.isGeneratedId = idFields.length == 1 && idFields[0].isAnnotationPresent(GeneratedValue.class);

        Class<?> idClass = BeanUtil.getIdClass(entityClass);
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
        SqlAndArgs sqlAndArgs = sqlBuilder.buildDeleteAndArgs(q);
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    public final E get(IdWrapper<I> w) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildSelectById(w, columnsForSelect);
        List<E> list = databaseOperations.query(sqlAndArgs, rowMapper);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public int delete(IdWrapper<I> w) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildDeleteById(w);
        return databaseOperations.update(sqlAndArgs);
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
        SqlAndArgs sqlAndArgs = sqlBuilder.buildCreateAndArgs(entities, columns);
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    public final int update(E e) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildUpdateAndArgs(e);
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    public final int patch(E e) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildPatchAndArgsWithId(e);
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    public final int patch(E e, Q q) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildPatchAndArgs(e, q);
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    public List<I> queryIds(Q query) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildSelectIdAndArgs(query);
        return databaseOperations.query(sqlAndArgs, new SingleColumnRowMapper<>());
    }

}
