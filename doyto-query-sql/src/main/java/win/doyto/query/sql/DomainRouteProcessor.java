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

import static win.doyto.query.sql.Constant.*;
import static win.doyto.query.util.CommonUtil.firstLetter;

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
        return buildClause(lastDomain, domains, domainIds, joinTables, argList, domainRoute);
    }

    private String[] prepareDomainIds(List<String> domains) {
        return domains.stream().map(domain -> String.format(joinIdFormat, domain)).toArray(String[]::new);
    }

    private String[] prepareJoinTables(List<String> domains) {
        return IntStream.range(0, domains.size() - 1)
                        .mapToObj(i -> String.format(joinTableFormat, domains.get(i), domains.get(i + 1)))
                        .toArray(String[]::new);
    }

    private String buildClause(String lastDomain, List<String> domains, String[] domainIds, String[] joinTables, List<Object> argList, DomainRoute domainRoute) {
        StringBuilder subQueryBuilder = new StringBuilder(ID);
        for (int i = joinTables.length - 1; i >= 0; i--) {
            appendClauseForDomain(subQueryBuilder, domainIds[i + 1], joinTables[i]);
            if (i > 0) {
                buildInnerJoinQuery(subQueryBuilder, i, domains, domainIds, argList, domainRoute);
                buildNextWhere(subQueryBuilder, domainIds[i]);
            }
        }
        buildQueryForLastDomain(subQueryBuilder, lastDomain, domainIds, argList, domainRoute);
        appendTailParenthesis(subQueryBuilder, joinTables.length);
        return subQueryBuilder.toString();
    }

    private void appendClauseForDomain(StringBuilder subQueryBuilder, String domainId, String joinTable) {
        subQueryBuilder.append(IN).append("(").append(SELECT)
                       .append(domainId)
                       .append(FROM)
                       .append(joinTable);
    }

    private void buildInnerJoinQuery(
            StringBuilder subQueryBuilder, int current, List<String> domains, String[] domainIds,
            List<Object> argList, DomainRoute domainRoute
    ) {
        Object domainQuery = CommonUtil.readField(domainRoute, String.format(QUERY_FIELD_FORMAT, domains.get(current)));
        if (!(domainQuery instanceof DoytoQuery)) {
            return;
        }
        Character domainAlias = firstLetter(domainIds[current]);
        String condition = BuildHelper.buildCondition(domainAlias + CONN, (DoytoQuery) domainQuery, argList);
        String joinTableAlias = EMPTY + firstLetter(domains.get(current)) + firstLetter(domains.get(current + 1));
        String table = String.format(tableFormat, domains.get(current));
        subQueryBuilder.append(SPACE).append(joinTableAlias)
                       .append(INNER_JOIN).append(table).append(SPACE).append(domainAlias)
                       .append(ON).append(domainAlias).append(CONN).append(ID)
                       .append(EQUAL).append(joinTableAlias).append(CONN).append(domainIds[current])
                       .append(AND)
                       .append(condition);
    }

    private StringBuilder buildNextWhere(StringBuilder subQueryBuilder, String domainId) {
        return subQueryBuilder.append(WHERE).append(domainId);
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
