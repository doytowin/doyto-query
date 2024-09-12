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
import win.doyto.query.annotation.View;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.AggregateQuery;
import win.doyto.query.core.Query;

import java.util.List;
import java.util.Map;
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

    public static SqlAndArgs buildSelectAndArgs(EntityMetadata entityMetadata, AggregateQuery aggregateQuery) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            StringBuilder sqlBuilder = buildSelect(entityMetadata, aggregateQuery, argList);
            sqlBuilder.append(buildOrderBy(aggregateQuery));
            return buildPaging(sqlBuilder.toString(), aggregateQuery);
        });
    }

    private static StringBuilder
    buildSelect(EntityMetadata entityMetadata, AggregateQuery aggregateQuery, List<Object> argList) {
        StringBuilder sqlBuilder = new StringBuilder();
        if (!entityMetadata.getWithViews().isEmpty()) {
            sqlBuilder.append(buildWithSql(entityMetadata.getWithViews(), argList, aggregateQuery.getWithMap()));
        }
        sqlBuilder.append(buildSqlForEntity(entityMetadata, aggregateQuery, argList));
        return sqlBuilder;
    }

    public static SqlAndArgs buildCountAndArgs(EntityMetadata entityMetadata, AggregateQuery aggregateQuery) {
        return SqlAndArgs.buildSqlWithArgs((argList -> SELECT + COUNT + FROM +
                OP + buildSelect(entityMetadata, aggregateQuery, argList) + CP));
    }

    private static String
    buildWithSql(List<View> withViews, List<Object> argList, Map<Class<?>, AggregateQuery> withMap) {
        StringJoiner withJoiner = new StringJoiner(SEPARATOR, "WITH ", SPACE);
        for (View view : withViews) {
            EntityMetadata withMeta = EntityMetadata.build(view.value());
            AggregateQuery aggregateQuery = withMap.get(view.value());
            if (aggregateQuery != null) {
                String withSQL = buildSqlForEntity(withMeta, aggregateQuery, argList).toString();
                String withName = GlobalConfiguration.formatTable(view.with());
                withJoiner.add(withName + AS + OP + withSQL + CP);
            }
        }
        return withJoiner.toString();
    }

    private static StringBuilder
    buildSqlForEntity(EntityMetadata entityMetadata, AggregateQuery aggregateQuery, List<Object> argList) {
        Query entityQuery = aggregateQuery.getQuery();
        String columns = BuildHelper.replaceExpressionInString(entityMetadata.getColumnsForSelect(), entityQuery, argList);
        StringBuilder sqlBuilder = new StringBuilder(SELECT).append(columns).append(FROM);

        if (entityMetadata.getNested() != null) {
            AggregateQuery nestedQuery = aggregateQuery.getWithMap().get(entityMetadata.getNested().getViewClass());
            if (nestedQuery != null) {
                String nestedSQL = buildSqlForEntity(entityMetadata.getNested(), nestedQuery, argList).toString();
                sqlBuilder.append(OP).append(nestedSQL).append(CP).append(AS);
            }
        }

        sqlBuilder.append(entityMetadata.getTableName());
        if (entityQuery != null) {
            buildJoinClauses(sqlBuilder, entityQuery, argList);
            if (entityMetadata.getJoinConditions().isEmpty()) {
                sqlBuilder.append(buildWhere(entityQuery, argList));
            } else {
                sqlBuilder.append(entityMetadata.getJoinConditions());
                sqlBuilder.append(buildCondition(AND, entityQuery, argList));
            }
        }
        sqlBuilder.append(entityMetadata.getGroupBySql());
        if (aggregateQuery.getHaving() != null) {
            sqlBuilder.append(buildCondition(HAVING, aggregateQuery.getHaving(), argList));
        }
        return sqlBuilder;
    }

}
