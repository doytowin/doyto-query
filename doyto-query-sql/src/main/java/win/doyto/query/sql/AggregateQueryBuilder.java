/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.annotation.Join;
import win.doyto.query.annotation.View;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.Having;
import win.doyto.query.core.PageQuery;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
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
public class AggregateQueryBuilder {

    public static SqlAndArgs buildSelectAndArgs(EntityMetadata entityMetadata, DoytoQuery aggregateQuery) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            StringBuilder sqlBuilder = buildSelect(entityMetadata, aggregateQuery, argList);
            sqlBuilder.append(buildOrderBy(aggregateQuery));
            return buildPaging(sqlBuilder.toString(), aggregateQuery);
        });
    }

    public static StringBuilder
    buildSelect(EntityMetadata entityMetadata, DoytoQuery aggregateQuery, List<Object> argList) {
        StringBuilder sqlBuilder = new StringBuilder();
        if (!entityMetadata.getWithViews().isEmpty()) {
            sqlBuilder.append(buildWithSql(entityMetadata.getWithViews(), argList, aggregateQuery));
        }
        sqlBuilder.append(buildSqlForEntity(entityMetadata, aggregateQuery, argList));
        return sqlBuilder;
    }

    public static SqlAndArgs buildCountAndArgs(EntityMetadata entityMetadata, DoytoQuery aggregateQuery) {
        return SqlAndArgs.buildSqlWithArgs((argList -> SELECT + COUNT + FROM +
                OP + buildSelect(entityMetadata, aggregateQuery, argList) + CP + " t"));
    }

    private static String
    buildWithSql(List<View> withViews, List<Object> argList, DoytoQuery query) {
        StringJoiner withJoiner = new StringJoiner(SEPARATOR, "WITH ", SPACE);
        for (View view : withViews) {
            String withSQL = buildSqlBody(argList, query, view.value());
            String withName = BuildHelper.defaultTableName(view.value());
            withJoiner.add(withName + AS + OP + withSQL + CP);
        }
        return withJoiner.toString();
    }

    private static String
    buildNestedSql(List<View> nestedViews, List<Object> argList, DoytoQuery query) {
        StringJoiner withJoiner = new StringJoiner(SEPARATOR);
        for (View view : nestedViews) {
            String nestedSQL = buildSqlBody(argList, query, view.value());
            String withName = BuildHelper.defaultTableName(view.value());
            withJoiner.add(OP + nestedSQL + CP + AS + withName);
        }
        return withJoiner.toString();
    }

    private static String buildSqlBody(List<Object> argList, DoytoQuery query, Class<?> viewClass) {
        EntityMetadata withMeta = EntityMetadata.build(viewClass);
        String viewName = StringUtils.uncapitalize(viewClass.getSimpleName());
        String queryFieldName = viewName.replace("View", "Query");
        DoytoQuery nestedQuery = (DoytoQuery) CommonUtil.readField(query, queryFieldName);
        if (nestedQuery == null) {
            nestedQuery = new PageQuery();
        }
        return buildSqlForEntity(withMeta, nestedQuery, argList).toString();
    }

    private static StringBuilder
    buildSqlForEntity(EntityMetadata entityMetadata, DoytoQuery aggregateQuery, List<Object> argList) {
        String columns = BuildHelper.replaceExpressionInString(entityMetadata.getColumnsForSelect(), aggregateQuery, argList);
        StringBuilder sqlBuilder = new StringBuilder(SELECT).append(columns).append(FROM);

        if (!entityMetadata.getNestedViews().isEmpty()) {
            sqlBuilder.append(buildNestedSql(entityMetadata.getNestedViews(), argList, aggregateQuery));
            if (!entityMetadata.getTableName().isEmpty()) {
                sqlBuilder.append(SEPARATOR);
            }
        }
        sqlBuilder.append(entityMetadata.getTableName());

        buildJoinClauses(sqlBuilder, aggregateQuery, argList);
        if (entityMetadata.getJoinConditions().isEmpty()) {
            sqlBuilder.append(buildWhere(aggregateQuery, argList));
        } else {
            sqlBuilder.append(entityMetadata.getJoinConditions());
            sqlBuilder.append(buildCondition(AND, aggregateQuery, argList));
        }
        sqlBuilder.append(entityMetadata.getGroupBySql());
        if (aggregateQuery instanceof Having) {
            sqlBuilder.append(buildHaving((Having) aggregateQuery, argList));
        }
        return sqlBuilder;
    }

    static void buildJoinClauses(StringBuilder sqlBuilder, DoytoQuery query, List<Object> argList) {
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
}
