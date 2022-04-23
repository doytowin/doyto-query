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
import win.doyto.query.core.DomainRoute;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.IntStream;

import static win.doyto.query.sql.Constant.*;

/**
 * DomainRouteProcessor
 *
 * @author f0rb on 2022-04-22
 */
class DomainRouteProcessor implements FieldProcessor.Processor {

    private static final String TABLE_FORMAT = "t_%s";
    private static final String JOIN_TABLE_FORMAT = "t_%s_and_%s";

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
            lastDomain = domains.get(domains.size() - 1);
        }
        return buildClause(lastDomain, domainIds, joinTables, argList, domainRoute);
    }

    private String[] prepareDomainIds(List<String> domains) {
        return domains.stream().map(domain -> domain + "Id").toArray(String[]::new);
    }

    private String[] prepareJoinTables(List<String> domains) {
        return IntStream.range(0, domains.size() - 1)
                        .mapToObj(i -> String.format(JOIN_TABLE_FORMAT, domains.get(i), domains.get(i + 1)))
                        .toArray(String[]::new);
    }

    private String buildClause(String lastDomain, String[] domainIds, String[] joinTables, List<Object> argList, DomainRoute domainRoute) {
        int joinCount = joinTables.length;
        StringBuilder subQueryBuilder = new StringBuilder("id");
        for (int i = joinCount; i > 0; i--) {
            subQueryBuilder.append(" IN (SELECT ")
                           .append(domainIds[i])
                           .append(FROM)
                           .append(joinTables[i - 1]);
            if (i > 1) {
                subQueryBuilder.append(WHERE).append(domainIds[i - 1]);
            }
        }

        Field[] fields = ColumnUtil.initFields(domainRoute.getClass(), FieldProcessor::init);
        for (Field field : fields) {
            if (field.getName().startsWith(lastDomain)) {
                Object value = CommonUtil.readField(field, domainRoute);
                if (value != null) {
                    if (value instanceof DoytoQuery) {
                        String table = String.format(TABLE_FORMAT, lastDomain);
                        String where = BuildHelper.buildWhere((DoytoQuery) value, argList);
                        subQueryBuilder.append(WHERE)
                                       .append(domainIds[0]).append(IN).append("(")
                                       .append(SELECT).append("id").append(FROM).append(table).append(where)
                                       .append(")");
                    } else {
                        String clause = FieldProcessor.execute(field, argList, value);
                        subQueryBuilder.append(WHERE).append(clause);
                    }
                    break;
                }
            }
        }

        for (int i = 0; i < joinCount; i++) {
            subQueryBuilder.append(")");
        }
        return subQueryBuilder.toString();
    }
}
