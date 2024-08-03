/*
 * Copyright © 2019-2024 Forb Yuan
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
import win.doyto.query.annotation.GeneratedValue;
import win.doyto.query.annotation.Id;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.core.PageList;
import win.doyto.query.entity.Persistable;
import win.doyto.query.jdbc.rowmapper.BeanPropertyRowMapper;
import win.doyto.query.jdbc.rowmapper.ColumnMapRowMapper;
import win.doyto.query.jdbc.rowmapper.RowMapper;
import win.doyto.query.jdbc.rowmapper.SingleColumnRowMapper;
import win.doyto.query.sql.EntityMetadata;
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
    private final SingleColumnRowMapper<I> idRowMapper;
    private final Class<I> idClass;
    private final String idColumn;

    public JdbcDataAccess(DatabaseOperations databaseOperations, Class<E> entityClass) {
        this(databaseOperations, entityClass, new BeanPropertyRowMapper<>(entityClass));
    }

    public JdbcDataAccess(DatabaseOperations databaseOperations, Class<E> entityClass, RowMapper<E> rowMapper) {
        classRowMapperMap.put(entityClass, rowMapper);
        this.databaseOperations = databaseOperations;
        this.rowMapper = rowMapper;
        this.sqlBuilder = SqlBuilderFactory.create(entityClass);
        this.columnsForSelect = EntityMetadata.buildViewColumns(entityClass).split(", ");

        Field[] idFields = FieldUtils.getFieldsWithAnnotation(entityClass, Id.class);
        this.isGeneratedId = idFields.length == 1 && idFields[0].isAnnotationPresent(GeneratedValue.class);
        this.idColumn = idFields[0].getName();
        this.idClass = BeanUtil.getIdClass(entityClass, idColumn);
        this.idRowMapper = new SingleColumnRowMapper<>(idClass);
    }

    @Override
    public List<E> query(Q q) {
        return queryColumns(q, rowMapper, columnsForSelect);
    }

    @Override
    public PageList<E> page(Q query) {
        query.forcePaging();
        return new PageList<>(query(query), count(query));
    }

    @Override
    public <V> List<V> queryColumns(Q q, Class<V> clazz, String... columns) {
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
    public long count(Q q) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildCountAndArgs(q);
        return databaseOperations.count(sqlAndArgs);
    }

    @Override
    public int delete(Q q) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildDeleteAndArgs(q);
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    public E get(IdWrapper<I> w) {
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
    public void create(E e) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildCreateAndArgs(e);

        if (isGeneratedId) {
            String keyColumn = GlobalConfiguration.dialect().resolveKeyColumn(idColumn);
            I id = databaseOperations.insert(sqlAndArgs, idClass, keyColumn).get(0);
            e.setId(id);
        } else {
            databaseOperations.update(sqlAndArgs);
        }
    }

    @Override
    public int batchInsert(Iterable<E> entities, String... columns) {
        if (!entities.iterator().hasNext()) {
            return 0;
        }
        if (GlobalConfiguration.dialect().supportMultiGeneratedKeys()) {
            SqlAndArgs sqlAndArgs = sqlBuilder.buildCreateAndArgs(entities, columns);
            String keyColumn = GlobalConfiguration.dialect().resolveKeyColumn(idColumn);
            List<I> ids = databaseOperations.insert(sqlAndArgs, idClass, keyColumn);
            int i = 0;
            for (E entity : entities) {
                entity.setId(ids.get(i++));
            }
            return ids.size();
        } else {
            SqlAndArgs sqlAndArgs = sqlBuilder.buildCreateAndArgs(entities, columns);
            return databaseOperations.update(sqlAndArgs);
        }
    }

    @Override
    public int update(E e) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildUpdateAndArgs(e);
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    public int patch(E e) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildPatchAndArgsWithId(e);
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    public int patch(E e, Q q) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildPatchAndArgs(e, q);
        return databaseOperations.update(sqlAndArgs);
    }

    @Override
    public List<I> queryIds(Q q) {
        SqlAndArgs sqlAndArgs = sqlBuilder.buildSelectIdAndArgs(q);
        return databaseOperations.query(sqlAndArgs, idRowMapper);
    }

}
