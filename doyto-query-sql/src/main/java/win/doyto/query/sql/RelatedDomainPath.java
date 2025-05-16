/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.relation.DomainPathDetail;
import win.doyto.query.relation.Relation;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.List;

import static win.doyto.query.relation.DomainPathDetail.buildBy;
import static win.doyto.query.sql.BuildHelper.resolveTableName;
import static win.doyto.query.sql.Constant.*;

/**
 * RelatedDomainPath
 *
 * @author f0rb on 2024/1/22
 */
@AllArgsConstructor
public class RelatedDomainPath {

    public static final String KEY_COLUMN = "MAIN_ENTITY_ID";
    private DomainPathDetail domainPathDetail;
    private EntityMetadata em;

    public RelatedDomainPath(Field joinField, Class<?> joinEntityClass) {
        this.domainPathDetail = buildDomainPath(joinField, resolveTableName(joinField.getDeclaringClass()));
        this.em = EntityMetadata.build(joinEntityClass);
    }

    public static DomainPathDetail buildDomainPath(Field joinField, String hostTableName) {
        DomainPathDetail domainPathDetail = buildBy(joinField.getAnnotation(DomainPath.class));

        List<Relation> relations = domainPathDetail.getRelations();
        Relation relation = domainPathDetail.getBaseRelation();
        if (relations.size() == 1 && relation.getAssociativeTable().equals(relations.get(0).getAssociativeTable())) {
            // one-to-many/one-to-one
            relation.setFk1(relations.get(0).getFk1());
            relations.clear();
        } else if (relations.isEmpty() && relation.getFk1().equals("id")) {// many-to-one
            relation.setAssociativeTable(hostTableName);
            relations.add(relation);
        }
        return domainPathDetail;
    }

    public static String buildRelationSQL(Class<?> clazz, String... path) {
        DomainPathDetail domainPathDetail = buildBy(path, "id", "id", ColumnUtil::convertColumn);
        RelatedDomainPath relatedDomainPath = new RelatedDomainPath(domainPathDetail, EntityMetadata.build(clazz));
        return relatedDomainPath.buildQueryForEachMainDomain().toString();
    }

    public StringBuilder buildQueryForEachMainDomain() {
        Relation baseRelation = domainPathDetail.getBaseRelation();
        // select columns from target entity
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SELECT).append(PLACE_HOLDER).append(AS).append(KEY_COLUMN)
                  .append(SEPARATOR).append(em.getColumnsForSelect())
                  .append(FROM).append(em.getTableName())
                  .append(WHERE_).append(baseRelation.getFk1());

        // build nested query for relations
        List<Relation> relations = domainPathDetail.getRelations();
        int n = relations.size();
        for (int i = 1; i <= n; i++) {
            Relation relation = relations.get(n - i);
            sqlBuilder.append(IN).append(OP).append(LF).append(SPACE + SPACE)
                      .append(SELECT).append(relation.getFk2())
                      .append(FROM).append(relation.getAssociativeTable())
                      .append(WHERE).append(relation.getFk1());
        }

        sqlBuilder.append(EQUAL_HOLDER);
        if (n > 0) sqlBuilder.append(LF).append(StringUtils.repeat(CP, n));
        return sqlBuilder;
    }

}
