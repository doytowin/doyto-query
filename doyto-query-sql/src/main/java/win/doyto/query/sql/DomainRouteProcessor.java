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
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.DomainRoute;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.IntStream;

import static win.doyto.query.sql.BuildHelper.buildWhere;
import static win.doyto.query.sql.Constant.*;

/**
 * DomainRouteProcessor
 *
 * @author f0rb on 2022-04-22
 */
class DomainRouteProcessor implements FieldProcessor.Processor {

    private static final String QUERY_FIELD_FORMAT = "%sQuery";
    private final String joinIdFormat = GlobalConfiguration.instance().getJoinIdFormat();
    private final String tableFormat = GlobalConfiguration.instance().getTableFormat();
    private final String joinTableFormat = GlobalConfiguration.instance().getJoinTableFormat();

    @Override
    public String process(List<Object> argList, Object value) {
        DomainRoute domainRoute = (DomainRoute) value;
        List<String> domains = domainRoute.getPath();
        String[] domainIds = prepareDomainIds(domains);
        String[] joinTables = prepareJoinTables(domains);
        String lastDomain;
        if (domainRoute.isReverse()) {
            lastDomain = domains.get(0);
        } else {
            ArrayUtils.reverse(domainIds);
            ArrayUtils.reverse(joinTables);
            lastDomain = domains.get(domains.size() - 1);
        }
        return buildClause(lastDomain, domains, domainIds, joinTables, domainRoute, argList);
    }

    private String[] prepareDomainIds(List<String> domains) {
        return domains.stream().map(domain -> String.format(joinIdFormat, domain)).toArray(String[]::new);
    }

    private String[] prepareJoinTables(List<String> domains) {
        return IntStream.range(0, domains.size() - 1)
                        .mapToObj(i -> String.format(joinTableFormat, domains.get(i), domains.get(i + 1)))
                        .toArray(String[]::new);
    }

    private String buildClause(
            String lastDomain, List<String> domains, String[] domainIds, String[] joinTables,
            DomainRoute domainRoute, List<Object> argList
    ) {
        int current = domainIds.length - 1;
        StringBuilder subQueryBuilder = new StringBuilder(ID);
        if (current > 0) {
            subQueryBuilder.append(IN).append("(");
            while (true) {
                buildStartForCurrentDomain(subQueryBuilder, domainIds[current], joinTables[current - 1]);
                if (--current <= 0) {
                    break;
                }
                buildWhereForCurrentDomain(subQueryBuilder, domainIds[current]);
                buildQueryForCurrentDomain(subQueryBuilder, domains.get(current), argList, domainRoute);
            }
        }
        buildQueryForLastDomain(subQueryBuilder, lastDomain, domainIds, argList, domainRoute);
        appendTailParenthesis(subQueryBuilder, joinTables.length);
        return subQueryBuilder.toString();
    }

    private void buildWhereForCurrentDomain(StringBuilder subQueryBuilder, String domainIds) {
        subQueryBuilder.append(WHERE).append(domainIds).append(IN).append("(");
    }

    private void buildStartForCurrentDomain(StringBuilder subQueryBuilder, String domainId, String joinTable) {
        subQueryBuilder.append(SELECT).append(domainId).append(FROM).append(joinTable);
    }

    @SuppressWarnings("unchecked")
    private void buildQueryForCurrentDomain(
            StringBuilder subQueryBuilder, String currentDomain,
            List<Object> argList, DomainRoute domainRoute
    ) {
        String queryFieldName = String.format(QUERY_FIELD_FORMAT, currentDomain);
        Object domainQuery = CommonUtil.readField(domainRoute, queryFieldName);
        if (!(domainQuery instanceof DoytoQuery)) {
            return;
        }
        String where = buildWhere((DoytoQuery) domainQuery, argList);
        String table = String.format(tableFormat, currentDomain);
        subQueryBuilder.append(SELECT).append(ID).append(FROM).append(table).append(where);
        subQueryBuilder.append(INTERSECT);
    }

    private void buildQueryForLastDomain(StringBuilder subQueryBuilder, String lastDomain, String[] domainIds, List<Object> argList, DomainRoute domainRoute) {
        Field[] fields = ColumnUtil.initFields(domainRoute.getClass(), FieldProcessor::init);
        for (Field field : fields) {
            if (field.getName().startsWith(lastDomain)) {
                Object value = CommonUtil.readField(field, domainRoute);
                if (value != null) {
                    if (value instanceof DoytoQuery) {
                        buildSubQueryForLastDomain(subQueryBuilder, lastDomain, domainIds, argList, domainRoute, (DoytoQuery) value);
                    } else {
                        buildWhereForLastDomain(subQueryBuilder, argList, field, value);
                    }
                    break;
                }
            }
        }
    }

    private void buildSubQueryForLastDomain(
            StringBuilder subQueryBuilder, String lastDomain, String[] domainIds,
            List<Object> argList, DomainRoute domainRoute, DoytoQuery value
    ) {
        String table = String.format(tableFormat, lastDomain);
        String where = BuildHelper.buildWhere(value, argList);
        if (domainIds.length > 1) {
            subQueryBuilder.append(WHERE).append(domainIds[0]);
        }
        subQueryBuilder.append(IN).append("(")
                       .append(SELECT).append(domainRoute.getLastDomainIdColumn()).append(FROM).append(table).append(where)
                       .append(")");
    }

    private void buildWhereForLastDomain(StringBuilder subQueryBuilder, List<Object> argList, Field field, Object value) {
        String clause = FieldProcessor.execute(field, argList, value);
        subQueryBuilder.append(WHERE).append(clause);
    }

    private void appendTailParenthesis(StringBuilder subQueryBuilder, int count) {
        subQueryBuilder.append(StringUtils.repeat(')', count));
    }
}
