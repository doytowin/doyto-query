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
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.AggregatedQuery;
import win.doyto.query.core.DoytoQuery;
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

    public static SqlAndArgs buildSelectAndArgs(Class<?> viewClass, AggregatedQuery having) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            EntityMetadata entityMetadata = EntityMetadata.build(viewClass);

            String sql = "";
            if (!entityMetadata.getWithViews().isEmpty()) {
                sql = buildWithSql(entityMetadata.getWithViews(), argList, having);
            }
            return sql + buildSqlForEntity(entityMetadata, having, argList);
        });
    }

    private static String buildWithSql(List<View> withViews, List<Object> argList, DoytoQuery query) {
        StringJoiner withJoiner = new StringJoiner(SEPARATOR, "WITH ", SPACE);
        for (View view : withViews) {
            EntityMetadata withMeta = EntityMetadata.build(view.value());
            String queryFieldName = StringUtils.uncapitalize(view.value().getSimpleName()).replace("View", "Query");
            AggregatedQuery withQuery = (AggregatedQuery) CommonUtil.readField(query, queryFieldName);
            String withSQL = buildSqlForEntity(withMeta, withQuery, argList);
            String withName = GlobalConfiguration.formatTable(view.with());
            withJoiner.add(withName + AS + OP + withSQL + CP);
        }
        return withJoiner.toString();
    }

    private static String buildSqlForEntity(EntityMetadata entityMetadata, AggregatedQuery aggrQuery, List<Object> argList) {
        DoytoQuery entityQuery;
        entityQuery = aggrQuery == null ? new PageQuery() : aggrQuery.getEntityQuery();
        String columns = BuildHelper.replaceExpressionInString(entityMetadata.getColumnsForSelect(), entityQuery, argList);
        StringBuilder sqlBuilder = new StringBuilder(SELECT).append(columns).append(FROM);

        if (entityMetadata.getNested() != null) {
            String queryFieldName = CommonUtil.toCamelCase(entityMetadata.getTableName()) + "Query";
            AggregatedQuery nestedQuery = (AggregatedQuery) CommonUtil.readField(entityQuery, queryFieldName);
            String nestedSQL = buildSqlForEntity(entityMetadata.getNested(), nestedQuery, argList);
            sqlBuilder.append(OP).append(nestedSQL).append(CP).append(AS);
        }

        sqlBuilder.append(entityMetadata.getTableName());
        if (entityQuery == null) {
            sqlBuilder.append(entityMetadata.getGroupBySql());
            return sqlBuilder.toString();
        }
        buildJoinClauses(sqlBuilder, entityQuery, argList);
        if (entityMetadata.getJoinConditions().isEmpty()) {
            sqlBuilder.append(buildWhere(entityQuery, argList));
        } else {
            sqlBuilder.append(entityMetadata.getJoinConditions());
            sqlBuilder.append(buildCondition(AND, entityQuery, argList));
        }
        if (aggrQuery != null) {
            sqlBuilder.append(entityMetadata.getGroupBySql());
            sqlBuilder.append(buildCondition(HAVING, aggrQuery, argList));
            sqlBuilder.append(buildOrderBy(aggrQuery));
            return buildPaging(sqlBuilder.toString(), aggrQuery);
        } else {
            return sqlBuilder.toString();
        }
    }

}
