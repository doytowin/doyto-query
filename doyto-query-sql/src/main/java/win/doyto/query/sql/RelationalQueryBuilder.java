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
import win.doyto.query.core.DoytoQuery;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
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

    public static SqlAndArgs buildSelectAndArgs(DoytoQuery query, EntityMetadata entityMetadata) {
        return SqlAndArgs.buildSqlWithArgs(argList -> buildSelect(query, entityMetadata, argList));
    }

    public static String buildSelect(DoytoQuery query, EntityMetadata entityMetadata, List<Object> argList) {
        String columns = BuildHelper.replaceExpressionInString(entityMetadata.getColumnsForSelect(), query, argList);
        StringBuilder sqlBuilder = new StringBuilder(SELECT).append(columns).append(FROM);
        sqlBuilder.append(entityMetadata.getTableName());
        if (query == null) {
            sqlBuilder.append(entityMetadata.getGroupBySql());
            return sqlBuilder.toString();
        }
        sqlBuilder.append(buildWhere(query, argList));
        sqlBuilder.append(buildOrderBy(query));
        return buildPaging(sqlBuilder.toString(), query);
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
