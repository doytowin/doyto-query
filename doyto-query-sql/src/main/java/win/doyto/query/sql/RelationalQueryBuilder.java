/*
 * Copyright © 2019-2023 Forb Yuan
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

package win.doyto.query.sql;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.AggregationQuery;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.Having;
import win.doyto.query.core.PageQuery;
import win.doyto.query.relation.DomainPathDetail;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static win.doyto.query.sql.BuildHelper.*;
import static win.doyto.query.sql.Constant.*;

/**
 * RelationalQueryBuilder
 *
 * @author f0rb on 2019-06-09
 */
@UtilityClass
public class RelationalQueryBuilder {

    public static final String KEY_COLUMN = "MAIN_ENTITY_ID";

    public static SqlAndArgs buildSelectAndArgs(DoytoQuery q, Class<?> entityClass) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            DoytoQuery query = SerializationUtils.clone(q);
            EntityMetadata entityMetadata = EntityMetadata.build(entityClass);
            return buildSqlForEntity(entityMetadata, query, argList);
        });
    }

    private static String buildSqlForEntity(EntityMetadata entityMetadata, DoytoQuery query, List<Object> argList) {
        String columns = BuildHelper.replaceExpressionInString(entityMetadata.getColumnsForSelect(), query, argList);
        StringBuilder sqlBuilder = new StringBuilder(SELECT).append(columns).append(FROM);

        if (entityMetadata.getNested() != null) {
            String queryFieldName = CommonUtil.toCamelCase(entityMetadata.getTableName() + "Query");
            DoytoQuery nestedQuery = (DoytoQuery) CommonUtil.readField(query, queryFieldName);
            String nestedSQL = buildSqlForEntity(entityMetadata.getNested(), nestedQuery, argList);
            sqlBuilder.append(OP).append(nestedSQL).append(CP).append(AS);
        }

        sqlBuilder.append(entityMetadata.getTableName());
        if (entityMetadata.getJoinConditions().isEmpty()) {
            sqlBuilder.append(buildWhere(query, argList));
        } else {
            sqlBuilder.append(entityMetadata.getJoinConditions());
            sqlBuilder.append(buildCondition(AND, query, argList));
        }
        sqlBuilder.append(entityMetadata.getGroupBySql());
        if (query instanceof AggregationQuery) {
            sqlBuilder.append(buildHaving(((AggregationQuery) query).getHaving(), argList));
        }
        sqlBuilder.append(buildOrderBy(query));
        return buildPaging(sqlBuilder.toString(), query);
    }

    private static String buildHaving(Having having, List<Object> argList) {
        if (having == null) {
            return EMPTY;
        }
        return buildCondition(" HAVING ", having, argList);
    }

    public static SqlAndArgs buildCountAndArgs(DoytoQuery q, Class<?> entityClass) {
        return SqlAndArgs.buildSqlWithArgs((argList -> {
            DoytoQuery query = SerializationUtils.clone(q);
            EntityMetadata entityMetadata = EntityMetadata.build(entityClass);
            String count = COUNT;
            String groupByColumns = entityMetadata.getGroupByColumns();
            if (!groupByColumns.isEmpty()) {
                count = "COUNT(DISTINCT(" + groupByColumns + "))";
            }
            return SELECT + count +
                    FROM + entityMetadata.getTableName() +
                    buildWhere(query, argList);
        }));
    }

    static <I extends Serializable, R> SqlAndArgs buildSqlAndArgsForSubDomain(Field joinField, List<I> mainIds, Class<R> joinEntityClass) {
        return buildSqlAndArgsForSubDomain(new PageQuery(), joinEntityClass, joinField, mainIds);
    }

    public static <I extends Serializable, R> SqlAndArgs buildSqlAndArgsForSubDomain(
            DoytoQuery query, Class<R> joinEntityClass, Field joinField, List<I> mainIds
    ) {
        DomainPath domainPath = joinField.getAnnotation(DomainPath.class);
        String[] domains = domainPath.value();
        String mainTableName = resolveTableName(joinField.getDeclaringClass());
        String subTableName = GlobalConfiguration.formatTable(domains[0]);
        String subColumns = buildSubDomainColumns(joinEntityClass);
        LinkedList<Object> queryArgs = new LinkedList<>();
        StringBuilder sqlBuilder;
        if (domains.length == 1) {
            if (Collection.class.isAssignableFrom(joinField.getType())) {
                sqlBuilder = buildQueryManyForEachMainDomain(domainPath.foreignField(), subTableName, subColumns);
            } else {
                sqlBuilder = buildQueryOneForEachMainDomain(mainTableName, domainPath.localField(), subTableName, subColumns);
            }
        } else {
            DomainPathDetail domainPathDetail = DomainPathDetail.buildBy(domainPath);
            sqlBuilder = buildQueryForEachMainDomain(subColumns, domainPathDetail);
        }

        String condition = buildCondition(AND, query, queryArgs);
        if (!condition.isEmpty()) {
            sqlBuilder.append(condition);
        }
        sqlBuilder.append(buildOrderBy(query, "\nORDER BY "));
        String clause = buildPaging(sqlBuilder.toString(), query);

        return buildSqlAndArgsForJoin(clause, mainIds, queryArgs);
    }

    private static <I extends Serializable> SqlAndArgs buildSqlAndArgsForJoin(
            String clause, List<I> mainIds, LinkedList<Object> queryArgs
    ) {
        return SqlAndArgs.buildSqlWithArgs(args -> mainIds
                .stream()
                .map(mainId -> {
                    args.add(mainId);
                    args.add(mainId);
                    args.addAll(queryArgs);
                    return clause;
                }).collect(Collectors.joining(UNION_ALL, LF, EMPTY))
        );
    }

    private static StringBuilder buildQueryManyForEachMainDomain(
            String mainFKColumn, String subTableName, String subColumns
    ) {
        return new StringBuilder()
                .append(SELECT).append(PLACE_HOLDER).append(AS).append(KEY_COLUMN).append(SEPARATOR).append(subColumns)
                .append(FROM).append(subTableName)
                .append(WHERE_).append(mainFKColumn).append(EQUAL_HOLDER);

    }

    private static StringBuilder buildQueryOneForEachMainDomain(
            String mainTableName, String mainFKColumn, String subTableName, String subColumns
    ) {
        return new StringBuilder()
                .append(SELECT).append(PLACE_HOLDER).append(AS).append(KEY_COLUMN).append(SEPARATOR).append(subColumns)
                .append(FROM).append(subTableName)
                .append(WHERE_).append(ID).append(EQUAL).append(OP).append(LF).append(SPACE).append(SPACE)
                .append(SELECT).append(mainFKColumn).append(FROM).append(mainTableName)
                .append(WHERE).append(ID).append(EQUAL_HOLDER).append(LF).append(CP);
    }

    private static StringBuilder buildQueryForEachMainDomain(
            String columns, DomainPathDetail domainPathDetail
    ) {
        String[] joinIds = domainPathDetail.getJoinIds();
        String[] joinTables = domainPathDetail.getJoinTables();
        String targetDomainTable = domainPathDetail.getTargetTable();
        int n = joinIds.length - 1;

        // select columns from target domain `joinTables[n]`
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SELECT).append(PLACE_HOLDER).append(AS).append(KEY_COLUMN)
                  .append(SEPARATOR).append(columns)
                  .append(FROM).append(targetDomainTable)
                  .append(WHERE_).append(ID);
        // nested query for medium domains
        for (int i = n - 1; i >= 0; i--) {
            sqlBuilder.append(IN).append(OP).append(LF).append(SPACE + SPACE)
                      .append(SELECT).append(joinIds[i + 1]).append(FROM).append(joinTables[i])
                      .append(WHERE).append(joinIds[i]);
        }
        sqlBuilder.append(EQUAL_HOLDER).append(LF).append(SPACE);
        IntStream.range(0, n).mapToObj(i -> CP).forEach(sqlBuilder::append);

        return sqlBuilder;
    }

    private static String buildSubDomainColumns(Class<?> joinEntityClass) {
        return FieldUtils.getAllFieldsList(joinEntityClass).stream()
                         .filter(RelationalQueryBuilder::filterForJoinEntity)
                         .map(ColumnUtil::selectAs)
                         .collect(Collectors.joining(SEPARATOR));
    }

    private static boolean filterForJoinEntity(Field field) {
        return ColumnUtil.shouldRetain(field)
                && !field.isAnnotationPresent(DomainPath.class)    // ignore join field
                ;
    }

}
