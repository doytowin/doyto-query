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

import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.relation.DomainPathDetail;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
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
    private final DomainPathDetail domainPathDetail;
    private final String[] domainIds;

    public DomainPathProcessor(Field field) {
        DomainPath domainPath = field.getAnnotation(DomainPath.class);
        boolean reverse = field.getName().contains(domainPath.value()[0]);
        domainPathDetail = DomainPathDetail.buildBy(domainPath, reverse);
        domainIds = domainPathDetail.getJoinIds();
    }

    @Override
    public String process(List<Object> argList, Object value) {
        return buildClause(argList, (DoytoQuery) value);
    }

    private String buildClause(List<Object> argList, DoytoQuery query) {
        String[] joinTables = domainPathDetail.getJoinTables();
        StringBuilder subQueryBuilder = new StringBuilder(domainPathDetail.getLocalFieldColumn());
        if (domainIds.length > 1) {
            int lastDomainIndex = domainIds.length - 1;
            int current = 0;
            subQueryBuilder.append(IN).append(OP);
            while (true) {
                buildStartForCurrentDomain(subQueryBuilder, domainIds[current], joinTables[current]);
                if (++current >= lastDomainIndex) {
                    break;
                }
                buildWhereForCurrentDomain(subQueryBuilder, domainIds[current]);
                buildQueryForCurrentDomain(subQueryBuilder, domainPathDetail.getDomainPath()[current], argList, query);
            }
        }
        buildQueryForLastDomain(subQueryBuilder, argList, query);
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
            StringBuilder subQueryBuilder, List<Object> argList, DoytoQuery query
    ) {
        int lastDomainIndex = domainIds.length - 1;
        if (domainIds.length > 1) {
            subQueryBuilder.append(WHERE).append(domainIds[lastDomainIndex]);
        }
        String where = BuildHelper.buildWhere(query, argList);
        subQueryBuilder.append(IN).append(OP)
                       .append(SELECT).append(domainPathDetail.getForeignFieldColumn())
                       .append(FROM).append(domainPathDetail.getTargetTable()).append(where)
                       .append(CP);
    }

    private void appendTailParenthesis(StringBuilder subQueryBuilder, int count) {
        subQueryBuilder.append(StringUtils.repeat(')', count));
    }
}
