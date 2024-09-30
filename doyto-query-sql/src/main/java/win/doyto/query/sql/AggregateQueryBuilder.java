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
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.View;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.Having;
import win.doyto.query.core.PageQuery;
import win.doyto.query.util.CommonUtil;

import java.util.List;
import java.util.StringJoiner;

import static win.doyto.query.sql.BuildHelper.*;
import static win.doyto.query.sql.Constant.*;
import static win.doyto.query.sql.RelationalQueryBuilder.buildJoinClauses;

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
    buildWithSql(List<View> withViews, List<Object> argList, DoytoQuery hostAggregateQuery) {
        StringJoiner withJoiner = new StringJoiner(SEPARATOR, "WITH ", SPACE);
        for (View view : withViews) {
            EntityMetadata withMeta = EntityMetadata.build(view.value());
            String viewName = StringUtils.uncapitalize(view.value().getSimpleName());
            String queryFieldName = viewName.replace("View", "Query");
            DoytoQuery withQuery = (DoytoQuery) CommonUtil.readField(hostAggregateQuery, queryFieldName);
            if (withQuery == null) {
                withQuery = new PageQuery();
            }
            String withSQL = buildSqlForEntity(withMeta, withQuery, argList).toString();
            String withName = BuildHelper.defaultTableName(view.value());
            withJoiner.add(withName + AS + OP + withSQL + CP);
        }
        return withJoiner.toString();
    }

    private static StringBuilder
    buildSqlForEntity(EntityMetadata entityMetadata, DoytoQuery aggregateQuery, List<Object> argList) {
        String columns = BuildHelper.replaceExpressionInString(entityMetadata.getColumnsForSelect(), aggregateQuery, argList);
        StringBuilder sqlBuilder = new StringBuilder(SELECT).append(columns).append(FROM);

        if (entityMetadata.getNested() != null) {
            String queryFieldName = CommonUtil.toCamelCase(entityMetadata.getTableName()) + "Query";
            DoytoQuery nestedQuery = (DoytoQuery) CommonUtil.readField(aggregateQuery, queryFieldName);
            if (nestedQuery == null) {
                nestedQuery = new PageQuery();
            }
            String nestedSQL = buildSqlForEntity(entityMetadata.getNested(), nestedQuery, argList).toString();
            sqlBuilder.append(OP).append(nestedSQL).append(CP).append(AS);
        }

        sqlBuilder.append(entityMetadata.getTableName());
        if (aggregateQuery != null) {
            buildJoinClauses(sqlBuilder, aggregateQuery, argList);
            if (entityMetadata.getJoinConditions().isEmpty()) {
                sqlBuilder.append(buildWhere(aggregateQuery, argList));
            } else {
                sqlBuilder.append(entityMetadata.getJoinConditions());
                sqlBuilder.append(buildCondition(AND, aggregateQuery, argList));
            }
        }
        sqlBuilder.append(entityMetadata.getGroupBySql());
        if (aggregateQuery instanceof Having having) {
            sqlBuilder.append(buildHaving(having, argList));
        }
        return sqlBuilder;
    }

}
