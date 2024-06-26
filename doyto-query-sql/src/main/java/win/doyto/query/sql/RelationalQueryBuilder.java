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

package win.doyto.query.sql;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.annotation.Join;
import win.doyto.query.annotation.View;
import win.doyto.query.core.AggregationQuery;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.Having;
import win.doyto.query.core.PageQuery;
import win.doyto.query.util.CommonUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

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
        return SqlAndArgs.buildSqlWithArgs(argList -> buildSelect(SerializationUtils.clone(q), entityClass, argList));
    }

    public static String buildSelect(DoytoQuery query, Class<?> entityClass, List<Object> argList) {
        EntityMetadata entityMetadata = EntityMetadata.build(entityClass);

        if (!entityMetadata.getWithViews().isEmpty()) {
            return buildWithSql(entityMetadata.getWithViews(), argList, query)
                    + buildSqlForEntity(entityMetadata, query, argList);
        }
        return buildSqlForEntity(entityMetadata, query, argList);

    }

    private static String buildWithSql(List<View> withViews, List<Object> argList, DoytoQuery query) {
        StringJoiner withJoiner = new StringJoiner(SEPARATOR,"WITH " , SPACE);
        for (View view : withViews) {
            EntityMetadata withMeta = EntityMetadata.build(view.value());
            String queryFieldName = StringUtils.uncapitalize(view.value().getSimpleName()).replace("View", "Query");
            DoytoQuery withQuery = (DoytoQuery) CommonUtil.readField(query, queryFieldName);
            String withSQL = buildSqlForEntity(withMeta, withQuery, argList);
            withJoiner.add(view.with() + AS + OP + withSQL + CP);
        }
        return withJoiner.toString();
    }

    private static String buildSqlForEntity(EntityMetadata entityMetadata, DoytoQuery query, List<Object> argList) {
        String columns = BuildHelper.replaceExpressionInString(entityMetadata.getColumnsForSelect(), query, argList);
        StringBuilder sqlBuilder = new StringBuilder(SELECT).append(columns).append(FROM);

        if (entityMetadata.getNested() != null) {
            String queryFieldName = CommonUtil.toCamelCase(entityMetadata.getTableName()) + "Query";
            DoytoQuery nestedQuery = (DoytoQuery) CommonUtil.readField(query, queryFieldName);
            String nestedSQL = buildSqlForEntity(entityMetadata.getNested(), nestedQuery, argList);
            sqlBuilder.append(OP).append(nestedSQL).append(CP).append(AS);
        }

        sqlBuilder.append(entityMetadata.getTableName());
        if (query == null) {
            sqlBuilder.append(entityMetadata.getGroupBySql());
            return sqlBuilder.toString();
        }
        buildJoinClauses(sqlBuilder, query, argList);
        if (entityMetadata.getJoinConditions().isEmpty()) {
            sqlBuilder.append(buildWhere(query, argList));
        } else {
            sqlBuilder.append(entityMetadata.getJoinConditions());
            sqlBuilder.append(buildCondition(AND, query, argList));
        }
        sqlBuilder.append(entityMetadata.getGroupBySql());
        if (query instanceof AggregationQuery aggregationQuery) {
            sqlBuilder.append(buildHaving(aggregationQuery.getHaving(), argList));
        }
        sqlBuilder.append(buildOrderBy(query));
        return buildPaging(sqlBuilder.toString(), query);
    }

    private static void buildJoinClauses(StringBuilder sqlBuilder, DoytoQuery query, List<Object> argList) {
        Field[] joinFields = FieldUtils.getFieldsWithAnnotation(query.getClass(), Join.class);
        for (Field field : joinFields) {
            Object joinObject = CommonUtil.readField(field, query);
            buildJoinClause(sqlBuilder, joinObject, argList, field.getAnnotation(Join.class));
        }
    }

    private static void buildJoinClause(StringBuilder sqlBuilder, Object joinQuery, List<Object> argList, Join join) {
        String joinType = join.type().getValue();
        View viewAnno = join.join();
        String hostTable = BuildHelper.resolveTableName(viewAnno);
        List<String> relations = EntityMetadata.resolveEntityRelations(new View[]{join.from(), viewAnno});
        String onConditions = relations.stream().collect(Collectors.joining(AND, ON, EMPTY));
        String andConditions = BuildHelper.buildCondition(AND, joinQuery, argList, viewAnno.alias());
        sqlBuilder.append(joinType)
                  .append(hostTable)
                  .append(onConditions)
                  .append(andConditions);
    }

    private static String buildHaving(Having having, List<Object> argList) {
        if (having == null) {
            return EMPTY;
        }
        return buildCondition(HAVING, having, argList);
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
        LinkedList<Object> queryArgs = new LinkedList<>();
        RelatedDomainPath relatedDomainPath = new RelatedDomainPath(joinField, joinEntityClass);
        StringBuilder sqlBuilder = relatedDomainPath.buildQueryForEachMainDomain();
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

}
