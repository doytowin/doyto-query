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

import lombok.NonNull;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.core.DataQueryClient;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.sql.JoinQueryBuilder;
import win.doyto.query.sql.SqlAndArgs;
import win.doyto.query.util.CommonUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JdbcDataQueryClient
 *
 * @author f0rb on 2021-12-28
 */
public class JdbcDataQueryClient implements DataQueryClient {

    private static final Map<Class<?>, RowMapper<?>> holder = new HashMap<>();
    private final DatabaseOperations databaseOperations;

    public JdbcDataQueryClient(JdbcOperations jdbcOperations) {
        this.databaseOperations = new DatabaseTemplate(jdbcOperations);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    List<V> query(Q query, @NonNull Class<V> viewClass) {
        RowMapper<V> rowMapper = (RowMapper<V>) holder.computeIfAbsent(viewClass, BeanPropertyRowMapper::new);
        SqlAndArgs sqlAndArgs = JoinQueryBuilder.buildSelectAndArgs(query, viewClass);
        List<V> mainEntities = databaseOperations.query(sqlAndArgs, rowMapper);
        querySubEntities(viewClass, mainEntities, query);
        return mainEntities;
    }

    @Override
    public <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    long count(Q query, Class<V> viewClass) {
        SqlAndArgs sqlAndArgs = JoinQueryBuilder.buildCountAndArgs(query, viewClass);
        return databaseOperations.count(sqlAndArgs);
    }

    private <V extends Persistable<I>, I extends Serializable, Q>
    void querySubEntities(Class<V> viewClass, List<V> mainEntities, Q query) {
        if (mainEntities.isEmpty()) {
            return;
        }
        // used for every subdomain query
        Class<I> mainIdClass = resolveKeyClass(mainEntities.get(0));
        List<I> mainIds = mainEntities.stream().map(Persistable::getId).collect(Collectors.toList());

        FieldUtils.getAllFieldsList(viewClass).stream()
                  .filter(joinField -> joinField.isAnnotationPresent(DomainPath.class))
                  .forEach(joinField -> {
                      // The name of query field for subdomain should follow this format `<joinFieldName>Query`
                      String queryFieldName = joinField.getName() + "Query";
                      Field queryField = CommonUtil.getField(query, queryFieldName);
                      if (queryField != null) {
                          Object subQuery = CommonUtil.readField(queryField, query);
                          if (subQuery instanceof DoytoQuery) {
                              if (List.class.isAssignableFrom(joinField.getType())) {
                                  queryEntitiesForJoinField(joinField, mainEntities, mainIds, (DoytoQuery) subQuery, mainIdClass);
                              } else {
                                  queryEntityForJoinField(joinField, mainEntities, mainIds, (DoytoQuery) subQuery, mainIdClass);
                              }
                          }
                      }
                  });
    }

    @SuppressWarnings("unchecked")
    private <E extends Persistable<I>, I extends Serializable> Class<I> resolveKeyClass(E e) {
        return (Class<I>) e.getId().getClass();
    }

    private <E extends Persistable<I>, I extends Serializable, R>
    void queryEntitiesForJoinField(Field joinField, List<E> mainEntities, List<I> mainIds, DoytoQuery query, Class<I> keyClass) {
        Class<R> joinEntityClass = resolveActualReturnClass(joinField);
        SqlAndArgs sqlAndArgs = JoinQueryBuilder.buildSqlAndArgsForSubDomain(query, joinEntityClass, joinField, mainIds);
        Map<I, List<R>> subDomainMap = queryIntoMainEntity(keyClass, joinEntityClass, sqlAndArgs);
        mainEntities.forEach(e -> writeResultToMainDomain(joinField, subDomainMap, e));
    }

    @SuppressWarnings("unchecked")
    private <R> Class<R> resolveActualReturnClass(Field field) {
        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
        Type[] actualTypeArguments = genericType.getActualTypeArguments();
        return (Class<R>) actualTypeArguments[0];
    }

    @SuppressWarnings("unchecked")
    private <I, R> Map<I, List<R>> queryIntoMainEntity(Class<I> keyClass, Class<R> joinEntityClass, SqlAndArgs sqlAndArgs) {
        RowMapper<R> joinRowMapper = (RowMapper<R>) holder.computeIfAbsent(joinEntityClass, BeanPropertyRowMapper::new);
        return databaseOperations.query(sqlAndArgs, new JoinRowMapperResultSetExtractor<>(keyClass, joinRowMapper));
    }

    private <E extends Persistable<I>, I extends Serializable, R>
    void writeResultToMainDomain(Field joinField, Map<I, List<R>> map, E e) {
        List<R> list = map.getOrDefault(e.getId(), new ArrayList<>());
        CommonUtil.writeField(joinField, e, list);
    }

    @SuppressWarnings("unchecked")
    private <E extends Persistable<I>, I extends Serializable, R>
    void queryEntityForJoinField(Field joinField, List<E> mainEntities, List<I> mainIds, DoytoQuery query, Class<I> keyClass) {
        Class<R> joinEntityClass = (Class<R>) joinField.getType();
        SqlAndArgs sqlAndArgs = JoinQueryBuilder.buildSqlAndArgsForSubDomain(query, joinEntityClass, joinField, mainIds);
        Map<I, List<R>> subDomainMap = queryIntoMainEntity(keyClass, joinEntityClass, sqlAndArgs);
        mainEntities.forEach(e -> writeSingleResultToMainDomain(joinField, subDomainMap, e));
    }

    private <E extends Persistable<I>, I extends Serializable, R>
    void writeSingleResultToMainDomain(Field joinField, Map<I, List<R>> map, E e) {
        List<R> list = map.getOrDefault(e.getId(), new ArrayList<>());
        CommonUtil.writeField(joinField, e, list.isEmpty() ? null : list.get(0));
    }
}
