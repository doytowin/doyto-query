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

import win.doyto.query.core.DomainRoute;

import java.util.List;
import java.util.stream.IntStream;

import static win.doyto.query.sql.Constant.FROM;
import static win.doyto.query.sql.Constant.WHERE;

/**
 * DomainRouteProcessor
 *
 * @author f0rb on 2022-04-22
 */
public class DomainRouteProcessor implements FieldProcessor.Processor {
    @Override
    public String process(List<Object> argList, Object value) {
        DomainRoute domainRoute = (DomainRoute) value;
        List<String> domains = domainRoute.getPath();
        String[] domainIds = prepareDomainIds(domains);
        String[] joinTables = prepareJoinTables(domains);
        return buildClause(domainIds, joinTables);
    }

    private String[] prepareDomainIds(List<String> domains) {
        return domains.stream().map(domain -> domain + "Id").toArray(String[]::new);
    }

    private String[] prepareJoinTables(List<String> domains) {
        return IntStream.range(0, domains.size() - 1)
                        .mapToObj(i -> String.format("t_%s_and_%s", domains.get(i), domains.get(i + 1)))
                        .toArray(String[]::new);
    }

    private String buildClause(String[] domainIds, String[] joinTables) {
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
        for (int i = 0; i < joinCount; i++) {
            subQueryBuilder.append(")");
        }
        return subQueryBuilder.toString();
    }
}
