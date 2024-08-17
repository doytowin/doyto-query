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

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.core.AggregationQuery;
import win.doyto.query.core.DataQueryClient;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.jdbc.rowmapper.BeanPropertyRowMapper;
import win.doyto.query.jdbc.rowmapper.JoinRowMapperResultSetExtractor;
import win.doyto.query.jdbc.rowmapper.RowMapper;
import win.doyto.query.sql.EntityMetadata;
import win.doyto.query.sql.SqlAndArgs;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static win.doyto.query.sql.RelationalQueryBuilder.*;

/**
 * JdbcDataQueryClient
 * <p>An JDBC implementation for {@link DataQueryClient}
 *
 * <p>History names:
 * <ul>
 * <li>JoinQueryExecutor
 * <li>JoinQueryService
 * <li>ComplexQueryService
 * <li>JdbcComplexQueryService
 * <li>JdbcComplexDataQuery
 * <li>JdbcDataQuery
 * </ul>
 *
 * @author f0rb on 2019-06-09
 * @since 0.1.3
 */
@AllArgsConstructor
public class JdbcDataQueryClient implements DataQueryClient {

    private static final Map<Class<?>, RowMapper<?>> holder = new ConcurrentHashMap<>();
    private final DatabaseOperations databaseOperations;

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    List<V> query(Q query, @NonNull Class<V> viewClass) {
        RowMapper<V> rowMapper = (RowMapper<V>) holder.computeIfAbsent(viewClass, BeanPropertyRowMapper::new);
        EntityMetadata entityMetadata = EntityMetadata.build(viewClass);
        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, entityMetadata);
        List<V> mainEntities = databaseOperations.query(sqlAndArgs, rowMapper);
        querySubEntities(mainEntities, query, ColumnUtil.resolveDomainPathFields(viewClass), EntityMetadata.build(viewClass));
        return mainEntities;
    }

    @Override
    public <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    long count(Q query, Class<V> viewClass) {
        SqlAndArgs sqlAndArgs = buildCountAndArgs(query, viewClass);
        return databaseOperations.count(sqlAndArgs);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V, Q extends DoytoQuery & AggregationQuery> List<V> aggregate(Q query, Class<V> viewClass) {
        RowMapper<V> rowMapper = (RowMapper<V>) holder.computeIfAbsent(viewClass, BeanPropertyRowMapper::new);
        SqlAndArgs sqlAndArgs = buildSelectAndArgs(query, viewClass);
        return databaseOperations.query(sqlAndArgs, rowMapper);
    }

    <V extends Persistable<I>, I extends Serializable, Q>
    void querySubEntities(List<V> mainEntities, Q query, List<Field> dpFields, EntityMetadata entityMetadata) {
        if (mainEntities.isEmpty()) {
            return;
        }
        // used for every subdomain query
        Class<I> mainIdClass = resolveKeyClass(mainEntities.get(0));
        List<I> mainIds = mainEntities.stream().map(Persistable::getId).toList();

        entityMetadata.getDomainPathFields().forEach(joinField -> {
            // The name of query field for subdomain should follow this format `with<JoinFieldName>`
            String queryFieldName = buildQueryFieldName(joinField);
            Object subQuery = CommonUtil.readField(query, queryFieldName);
            if (subQuery instanceof DoytoQuery) {
                boolean isList = Collection.class.isAssignableFrom(joinField.getType());
                Class<Object> joinEntityClass = resolveSubEntityClass(joinField, isList);
                SqlAndArgs sqlAndArgs = buildSqlAndArgsForSubDomain((DoytoQuery) subQuery, joinEntityClass, joinField, mainIds);
                Map<I, List<Object>> subDomainMap = queryIntoMainEntity(mainIdClass, joinEntityClass, sqlAndArgs);
                mainEntities.forEach(e -> writeResultToMainDomain(e, joinField, isList, subDomainMap));
            }
        });
    }

    protected String buildQueryFieldName(Field joinField) {
        return "with" + StringUtils.capitalize(joinField.getName());
    }

    @SuppressWarnings("unchecked")
    private <E extends Persistable<I>, I extends Serializable> Class<I> resolveKeyClass(E e) {
        return (Class<I>) e.getId().getClass();
    }

    @SuppressWarnings("unchecked")
    private static <R> Class<R> resolveSubEntityClass(Field joinField, boolean isList) {
        return isList ? CommonUtil.resolveActualReturnClass(joinField) : (Class<R>) joinField.getType();
    }

    @SuppressWarnings("unchecked")
    private <I, R> Map<I, List<R>> queryIntoMainEntity(Class<I> keyClass, Class<R> joinEntityClass, SqlAndArgs sqlAndArgs) {
        RowMapper<R> joinRowMapper = (RowMapper<R>) holder.computeIfAbsent(joinEntityClass, BeanPropertyRowMapper::new);
        JoinRowMapperResultSetExtractor<I, R> resultSetExtractor =
                new JoinRowMapperResultSetExtractor<>(KEY_COLUMN, keyClass, joinRowMapper);
        return databaseOperations.query(sqlAndArgs, resultSetExtractor);
    }

    private <E extends Persistable<I>, I extends Serializable, R>
    void writeResultToMainDomain(E e, Field joinField, boolean isList, Map<I, List<R>> map) {
        List<R> list = map.getOrDefault(e.getId(), new ArrayList<>());
        if (isList) {
            CommonUtil.writeField(joinField, e, list);
        } else if (!list.isEmpty()) {
            CommonUtil.writeField(joinField, e, list.get(0));
        }
    }
}
