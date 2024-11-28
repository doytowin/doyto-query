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

import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.relation.DomainPathDetail;
import win.doyto.query.relation.Relation;
import win.doyto.query.sql.BuildHelper;
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
class DomainPathProcessor implements FieldProcessor {
    private final DomainPathDetail domainPathDetail;

    public DomainPathProcessor(Field field) {
        DomainPath domainPath = field.getAnnotation(DomainPath.class);
        domainPathDetail = DomainPathDetail.buildBy(domainPath);
    }

    @Override
    public String process(String alias, List<Object> argList, Object value) {
        return buildClause(argList, (DoytoQuery) value);
    }

    private String buildClause(List<Object> argList, DoytoQuery query) {
        String[] domainPath = domainPathDetail.getDomainPath();
        Relation baseRelation = domainPathDetail.getBaseRelation();

        StringBuilder subQueryBuilder = new StringBuilder(baseRelation.getFk2()).append(IN).append(OP);
        for (int i = 0; i < domainPath.length - 1; i++) {
            String queryName = domainPath[i] + "Query";
            if (CommonUtil.readField(query, queryName) instanceof DoytoQuery domainQuery) {
                String table = GlobalConfiguration.formatTable(domainPath[i]);
                String where = buildWhere(domainQuery, argList);
                subQueryBuilder.append(SELECT).append(ID).append(FROM).append(table)
                               .append(where).append(INTERSECT);
            }
            Relation relation = domainPathDetail.getRelations().get(i);
            subQueryBuilder.append(SELECT).append(relation.getFk1())
                           .append(FROM).append(relation.getAssociativeTable())
                           .append(WHERE).append(relation.getFk2())
                           .append(IN).append(OP);
        }

        String where = BuildHelper.buildWhere(query, argList);
        subQueryBuilder.append(SELECT).append(baseRelation.getFk1())
                       .append(FROM).append(baseRelation.getAssociativeTable()).append(where)
                       .append(StringUtils.repeat(')', domainPath.length));
        return subQueryBuilder.toString();
    }
}
