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

package win.doyto.query.sql;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.util.ColumnUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import static win.doyto.query.sql.BuildHelper.*;
import static win.doyto.query.sql.Constant.*;
import static win.doyto.query.util.CommonUtil.CLT_COMMA_WITH_PAREN;

/**
 * JoinQueryBuilder
 *
 * @author f0rb on 2019-06-09
 */
@UtilityClass
public class JoinQueryBuilder {

    public static final String KEY_COLUMN = "PK_FOR_JOIN";

    public static SqlAndArgs buildSelectAndArgs(DoytoQuery q, Class<?> entityClass) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            DoytoQuery query = SerializationUtils.clone(q);
            EntityMetadata entityMetadata = EntityMetadata.build(entityClass);
            String sql = SELECT + entityMetadata.getColumnsForSelect() +
                    FROM + entityMetadata.getTableName() +
                    entityMetadata.resolveJoinSql(query, argList) +
                    buildWhere(query, argList) +
                    entityMetadata.getGroupBySql() +
                    buildOrderBy(query);
            return buildPaging(sql, query);
        });
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
                    entityMetadata.resolveJoinSql(query, argList) +
                    buildWhere(query, argList);
        }));
    }

    public static <I extends Serializable, R> SqlAndArgs buildSqlAndArgsForSubDomain(Field joinField, List<I> mainIds, Class<R> joinEntityClass) {

        DomainPath domainPath = joinField.getAnnotation(DomainPath.class);
        String[] domains = domainPath.value();
        int size = domains.length;
        int n = size - 1;
        String[] joinTables = new String[size];
        String[] joinAliases = new String[size];
        String[] joinIds = new String[size];
        for (int i = 0; i < n; i++) {
            joinIds[i] = String.format("%s_id", domains[i]);
            joinTables[i] = String.format("j_%s_and_%s", domains[i], domains[i + 1]);
            joinAliases[i] = String.format("j%d%c%c", i, domains[i].charAt(0), domains[i + 1].charAt(0));
        }
        String target = domains[n];
        joinTables[n] = String.format("t_%s", target);
        joinAliases[n] = target.substring(0, 1);
        joinIds[n] = String.format("%s_id", target);

        String columns = buildSubDomainColumns(joinEntityClass, joinAliases[n]);
        String subDomainId = ColumnUtil.resolveIdColumn(joinEntityClass);
        String mainIdsArg = mainIds.stream().map(Object::toString).collect(CLT_COMMA_WITH_PAREN);

        String sql = SELECT + joinAliases[0] + CONN + joinIds[0] + AS + KEY_COLUMN + SEPARATOR + columns + LF
                + FROM + joinTables[0] + SPACE + joinAliases[0] + LF
                + INNER_JOIN + joinTables[1] + SPACE + joinAliases[1]
                + ON + joinAliases[0] + CONN + joinIds[1];

        StringBuilder innerJoinSB = new StringBuilder();
        for (int i = 1; i < n; i++) {
            innerJoinSB.append(EQUAL).append(joinAliases[i]).append(CONN).append(joinIds[i]).append(LF)
                       .append(INNER_JOIN).append(joinTables[i + 1]).append(SPACE).append(joinAliases[i + 1])
                       .append(ON).append(joinAliases[i]).append(CONN).append(joinIds[i + 1]);
        }
        sql = sql + innerJoinSB + EQUAL + joinAliases[n] + CONN + subDomainId
                + LF + WHERE + joinAliases[0] + CONN + joinIds[0] + IN + mainIdsArg;

        return new SqlAndArgs(sql);
    }

    private static <R> String buildSubDomainColumns(Class<R> joinEntityClass, String joinAlias) {
        return FieldUtils.getAllFieldsList(joinEntityClass).stream()
                         .filter(JoinQueryBuilder::filterForJoinEntity)
                         .map(ColumnUtil::selectAs)
                         .map(col -> joinAlias + CONN + col)
                         .collect(Collectors.joining(SEPARATOR));
    }

    private static boolean filterForJoinEntity(Field field) {
        return ColumnUtil.shouldRetain(field)
                && !field.isAnnotationPresent(DomainPath.class)    // ignore join field
                ;
    }
}
