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

    public static SqlAndArgs buildSelectAndArgs(DoytoQuery query, EntityMetadata entityMetadata) {
        return SqlAndArgs.buildSqlWithArgs(argList -> buildSelect(query, entityMetadata, argList));
    }

    public static String buildSelect(DoytoQuery query, EntityMetadata entityMetadata, List<Object> argList) {
        String sql = "";
        if (!entityMetadata.getWithViews().isEmpty()) {
            sql = buildWithSql(entityMetadata.getWithViews(), argList, query);
        }
        return sql + buildSqlForEntity(entityMetadata, query, argList);
    }

    private static String buildWithSql(List<View> withViews, List<Object> argList, DoytoQuery query) {
        StringJoiner withJoiner = new StringJoiner(SEPARATOR, "WITH ", SPACE);
        for (View view : withViews) {
            EntityMetadata withMeta = EntityMetadata.build(view.value());
            String queryFieldName = StringUtils.uncapitalize(view.value().getSimpleName()).replace("View", "Query");
            DoytoQuery withQuery = (DoytoQuery) CommonUtil.readField(query, queryFieldName);
            String withSQL = buildSqlForEntity(withMeta, withQuery, argList);
            String withName = BuildHelper.defaultTableName(view.value());
            withJoiner.add(withName + AS + OP + withSQL + CP);
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
        sqlBuilder.append(buildOrderBy(query));
        return buildPaging(sqlBuilder.toString(), query);
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

    public static SqlAndArgs buildCountAndArgs(DoytoQuery query, Class<?> entityClass) {
        return SqlAndArgs.buildSqlWithArgs((argList -> {
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

    public static <I extends Serializable, R> SqlAndArgs buildSqlAndArgsForSubDomain(
            DoytoQuery query, Class<R> joinViewClass, Field joinField, List<I> mainIds
    ) {
        LinkedList<Object> queryArgs = new LinkedList<>();
        RelatedDomainPath relatedDomainPath = new RelatedDomainPath(joinField, joinViewClass);
        StringBuilder sqlBuilder = relatedDomainPath.buildQueryForEachMainDomain();
        String condition = buildCondition(AND, query, queryArgs);
        if (!condition.isEmpty()) {
            sqlBuilder.append(condition);
        }
        sqlBuilder.append(buildOrderBy(query, "\nORDER BY "));
        String clause = buildPaging(sqlBuilder.toString(), query);
        if (query.needPaging()) {
            clause = OP + clause + CP;
        }
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
