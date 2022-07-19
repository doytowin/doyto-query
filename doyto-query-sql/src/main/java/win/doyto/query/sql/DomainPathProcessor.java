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
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static win.doyto.query.config.GlobalConfiguration.*;
import static win.doyto.query.sql.BuildHelper.buildWhere;
import static win.doyto.query.sql.Constant.*;

/**
 * DomainPathProcessor
 *
 * @author f0rb on 2022-05-10
 * @since 0.3.1
 */
class DomainPathProcessor implements FieldProcessor.Processor {
    private final String[] domainPaths;
    private final String[] domainIds;
    private final String[] joinTables;
    private final String lastDomain;
    private final String lastDomainIdColumn;
    private final String localFieldColumn;

    public DomainPathProcessor(Field field) {
        DomainPath domainPath = field.getAnnotation(DomainPath.class);
        domainPaths = domainPath.value();
        lastDomainIdColumn = domainPath.lastDomainIdColumn();
        localFieldColumn = ColumnUtil.convertColumn(domainPath.localField());
        boolean reverse = field.getName().contains(domainPaths[0]);
        domainIds = prepareDomainIds();
        joinTables = prepareJoinTables();
        if (reverse) {
            lastDomain = domainPaths[0];
        } else {
            ArrayUtils.reverse(domainIds);
            ArrayUtils.reverse(joinTables);
            lastDomain = domainPaths[domainPaths.length - 1];
        }
    }

    private String[] prepareDomainIds() {
        return Arrays.stream(domainPaths).map(domain -> String.format(JOIN_ID_FORMAT, domain)).toArray(String[]::new);
    }

    private String[] prepareJoinTables() {
        return IntStream.range(0, domainPaths.length - 1)
                .mapToObj(i -> String.format(JOIN_TABLE_FORMAT, domainPaths[i], domainPaths[i + 1]))
                .toArray(String[]::new);
    }

    @Override
    public String process(List<Object> argList, Object value) {
        return buildClause(argList, (DoytoQuery) value);
    }

    private String buildClause(List<Object> argList, DoytoQuery query) {
        int current = domainIds.length - 1;
        StringBuilder subQueryBuilder = new StringBuilder(localFieldColumn);
        if (current > 0) {
            subQueryBuilder.append(IN).append(OP);
            while (true) {
                buildStartForCurrentDomain(subQueryBuilder, domainIds[current], joinTables[current - 1]);
                if (--current <= 0) {
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
            subQueryBuilder.append(WHERE).append(domainIds[0]);
        }
        String table = String.format(TABLE_FORMAT, lastDomain);
        String where = BuildHelper.buildWhere(query, argList);
        subQueryBuilder.append(IN).append(OP)
                       .append(SELECT).append(lastDomainIdColumn).append(FROM).append(table).append(where)
                       .append(CP);
    }

    private void appendTailParenthesis(StringBuilder subQueryBuilder, int count) {
        subQueryBuilder.append(StringUtils.repeat(')', count));
    }
}
