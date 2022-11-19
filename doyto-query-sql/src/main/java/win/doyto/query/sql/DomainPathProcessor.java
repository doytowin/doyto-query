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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static win.doyto.query.sql.BuildHelper.buildWhere;
import static win.doyto.query.sql.Constant.*;

/**
 * DomainPathProcessor
 *
 * @author f0rb on 2022-05-10
 * @since 0.3.1
 */
class DomainPathProcessor implements FieldProcessor.Processor {
    private static final String TABLE_FORMAT = GlobalConfiguration.instance().getTableFormat();
    private static final String REVERSE_SIGN = "~";
    private final String[] domainPaths;
    private final String[] domainIds;
    private final String[] joinTables;
    private final String lastDomain;
    private final String foreignFieldColumn;
    private final String localFieldColumn;
    private final int lastDomainIndex;

    public DomainPathProcessor(Field field) {
        DomainPath domainPath = field.getAnnotation(DomainPath.class);
        domainPaths = domainPath.value();
        foreignFieldColumn = ColumnUtil.convertColumn(domainPath.foreignField());
        localFieldColumn = ColumnUtil.convertColumn(domainPath.localField());
        domainIds = prepareDomainIds(GlobalConfiguration.instance().getJoinIdFormat());
        lastDomainIndex = domainIds.length - 1;

        joinTables = prepareJoinTablesWithReverseSign(domainPaths);
        if (!Arrays.asList(domainPaths).contains(REVERSE_SIGN) && field.getName().contains(domainPaths[0])) {
            ArrayUtils.reverse(domainIds);
            ArrayUtils.reverse(joinTables);
            ArrayUtils.reverse(domainPaths);
        }
        lastDomain = domainPaths[domainPaths.length - 1];
    }

    /**
     * Transform domain paths to join-tables
     * <p>
     * Examples:
     * <ul>
     *   <li>["perm", "~", "role"] -> ["perm_id","role_id"]/["a_role_and_perm"]</li>
     *   <li>["perm", "~", "role", "~", "user"] -> [ "perm_id","role_id","user_id"]/["a_role_and_perm","a_user_and_role"]</li>
     * </ul>
     * </p>
     */
    private static String[] prepareJoinTablesWithReverseSign(String[] domainPaths) {
        String joinTableFormat = GlobalConfiguration.instance().getJoinTableFormat();
        List<String> joinTableList = new ArrayList<>();
        int i = 0;
        while (i < domainPaths.length - 1) {
            String domain = domainPaths[i];
            String nextDomain = domainPaths[i + 1];
            if (REVERSE_SIGN.equals(nextDomain)) {
                nextDomain = domain;
                domain = domainPaths[i + 2];
                i++;
            }
            joinTableList.add(String.format(joinTableFormat, domain, nextDomain));
            i++;
        }
        return joinTableList.toArray(new String[0]);
    }

    private String[] prepareDomainIds(String joinIdFormat) {
        return Arrays.stream(domainPaths)
                     .filter(path -> !REVERSE_SIGN.equals(path))
                     .map(domain -> String.format(joinIdFormat, domain))
                     .toArray(String[]::new);
    }

    @Override
    public String process(List<Object> argList, Object value) {
        return buildClause(argList, (DoytoQuery) value);
    }

    private String buildClause(List<Object> argList, DoytoQuery query) {
        StringBuilder subQueryBuilder = new StringBuilder(localFieldColumn);
        if (domainIds.length > 1) {
            int current = 0;
            subQueryBuilder.append(IN).append(OP);
            while (true) {
                buildStartForCurrentDomain(subQueryBuilder, domainIds[current], joinTables[current]);
                if (++current >= lastDomainIndex) {
                    break;
                }
                buildWhereForCurrentDomain(subQueryBuilder, domainIds[current]);
                buildQueryForCurrentDomain(subQueryBuilder, domainPaths[current], argList, query);
            }
        }
        buildQueryForLastDomain(subQueryBuilder, lastDomain, argList, query);
        appendTailParenthesis(subQueryBuilder, joinTables.length);
        return subQueryBuilder.toString();
    }

    private void buildWhereForCurrentDomain(StringBuilder subQueryBuilder, String domainId) {
        subQueryBuilder.append(WHERE).append(domainId).append(IN).append(OP);
    }

    private void buildStartForCurrentDomain(StringBuilder subQueryBuilder, String domainId, String joinTable) {
        subQueryBuilder.append(SELECT).append(domainId).append(FROM).append(joinTable);
    }

    private void buildQueryForCurrentDomain(
            StringBuilder subQueryBuilder, String currentDomain,
            List<Object> argList, DoytoQuery query
    ) {
        Object domainQuery = CommonUtil.readField(query, currentDomain + "Query");
        if (!(domainQuery instanceof DoytoQuery)) {
            return;
        }
        String where = buildWhere((DoytoQuery) domainQuery, argList);
        String table = String.format(TABLE_FORMAT, currentDomain);
        subQueryBuilder.append(SELECT).append(ID).append(FROM).append(table).append(where);
        subQueryBuilder.append(INTERSECT);
    }

    private void buildQueryForLastDomain(
            StringBuilder subQueryBuilder, String lastDomain,
            List<Object> argList, DoytoQuery query
    ) {
        if (domainIds.length > 1) {
            subQueryBuilder.append(WHERE).append(domainIds[lastDomainIndex]);
        }
        String table = String.format(TABLE_FORMAT, lastDomain);
        String where = BuildHelper.buildWhere(query, argList);
        subQueryBuilder.append(IN).append(OP)
                       .append(SELECT).append(foreignFieldColumn).append(FROM).append(table).append(where)
                       .append(CP);
    }

    private void appendTailParenthesis(StringBuilder subQueryBuilder, int count) {
        subQueryBuilder.append(StringUtils.repeat(')', count));
    }
}
