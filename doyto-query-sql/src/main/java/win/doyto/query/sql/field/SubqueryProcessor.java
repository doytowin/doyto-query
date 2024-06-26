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

package win.doyto.query.sql.field;

import win.doyto.query.annotation.Subquery;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.AggregationQuery;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.Having;
import win.doyto.query.sql.BuildHelper;
import win.doyto.query.sql.EntityMetadata;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static win.doyto.query.sql.BuildHelper.buildCondition;
import static win.doyto.query.sql.Constant.*;

/**
 * SubqueryProcessor
 *
 * @author f0rb on 2022/12/29
 * @since 1.0.1
 */
public class SubqueryProcessor implements FieldProcessor {
    private static final Pattern PTN_SUBQUERY = Pattern.compile("^(\\w+)\\$(\\w+)From(\\w+)$");
    private final String clauseFormat;
    private String joinConditions = EMPTY;
    private String groupBy = EMPTY;

    public SubqueryProcessor(Field field) {
        Subquery subquery = field.getAnnotation(Subquery.class);
        String fieldName = BuildHelper.resolveFieldName(field.getName());
        String tableName = BuildHelper.resolveTableName(subquery.from());

        if (subquery.distinct()) {
            groupBy = GROUP_BY + subquery.select();
        }
        Class<?>[] classes = combineArray(subquery.host(), subquery.from());
        List<String> relations = EntityMetadata.resolveEntityRelations(classes);
        joinConditions = String.join(AND, relations);

        String column = subquery.select();
        if (!column.contains("(")) {
            column = ColumnUtil.resolveColumn(column);
        }
        clauseFormat = buildClauseFormat(fieldName, column, tableName);
    }

    private static Class<?>[] combineArray(Class<?>[] host, Class<?>[] from) {
        Class<?>[] classes = new Class<?>[host.length + from.length];
        System.arraycopy(host, 0, classes, 0, host.length);
        System.arraycopy(from, 0, classes, host.length, from.length);
        return classes;
    }

    public SubqueryProcessor(String originFieldName) {
        Matcher matcher = SubqueryProcessor.matches(originFieldName);
        assert matcher != null;
        String fieldName = matcher.group(1);
        String column = ColumnUtil.resolveColumn(matcher.group(2));
        String domain = ColumnUtil.convertColumn(matcher.group(3));
        String table = GlobalConfiguration.formatTable(domain);

        clauseFormat = buildClauseFormat(fieldName, column, table);
    }

    public static Matcher matches(String fieldName) {
        Matcher matcher = PTN_SUBQUERY.matcher(fieldName);
        return matcher.find() ? matcher : null;
    }

    static String buildClauseFormat(String fieldName, String column, String table) {
        String cond = resolveCond(fieldName);
        return cond + OP + SELECT + column + FROM + table + "%s" + CP;
    }

    /**
     * Cond contains the first two parts of the condition.
     *
     * @param fieldName name of the annotated field
     * @return column + operator
     */
    static String resolveCond(String fieldName) {
        String cond;
        SqlQuerySuffix querySuffix = SqlQuerySuffix.resolve(fieldName);
        if (querySuffix == SqlQuerySuffix.Any || querySuffix == SqlQuerySuffix.All) {
            String tempName = querySuffix.removeSuffix(fieldName);

            SqlQuerySuffix querySuffix1 = SqlQuerySuffix.resolve(tempName);
            String columnName = querySuffix1.removeSuffix(tempName);
            columnName = ColumnUtil.convertColumn(columnName);

            cond = columnName + querySuffix1.getOp() + querySuffix.getOp();
        } else {
            String columnName = querySuffix.removeSuffix(fieldName);
            columnName = ColumnUtil.convertColumn(columnName);
            cond = columnName + querySuffix.getOp();
        }
        return cond;
    }

    @Override
    public String process(String alias, List<Object> argList, Object value) {
        String clause;
        if (!joinConditions.isEmpty()) {
            clause = WHERE + joinConditions + BuildHelper.buildCondition(AND, value, argList);
        } else {
            clause = BuildHelper.buildWhere((DoytoQuery) value, argList);
        }
        clause += groupBy;
        if (value instanceof AggregationQuery aggregationQuery) {
            Having having = aggregationQuery.getHaving();
            if (having != null) {
                clause += buildCondition(HAVING, having, argList);
            }
        }
        return String.format(clauseFormat, clause);
    }
}
