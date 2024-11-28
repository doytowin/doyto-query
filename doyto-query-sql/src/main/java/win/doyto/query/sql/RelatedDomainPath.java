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

package win.doyto.query.sql;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.relation.DomainPathDetail;
import win.doyto.query.relation.Relation;

import java.lang.reflect.Field;
import java.util.List;

import static win.doyto.query.sql.BuildHelper.resolveTableName;
import static win.doyto.query.sql.Constant.*;
import static win.doyto.query.sql.RelationalQueryBuilder.KEY_COLUMN;

/**
 * RelatedDomainPath
 *
 * @author f0rb on 2024/1/22
 */
@AllArgsConstructor
public class RelatedDomainPath {

    private DomainPathDetail domainPathDetail;
    private String mainTableName;
    private String targetColumns;

    public RelatedDomainPath(Field joinField, Class<?> joinEntityClass) {
        this.domainPathDetail = DomainPathDetail.buildBy(joinField.getAnnotation(DomainPath.class));
        this.mainTableName = resolveTableName(joinField.getDeclaringClass());
        this.targetColumns = EntityMetadata.buildViewColumns(joinEntityClass);
    }


    public StringBuilder buildQueryForEachMainDomain() {
        Relation baseRelation = domainPathDetail.getBaseRelation();
        // select columns from target domain `joinTables[n]`
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SELECT).append(PLACE_HOLDER).append(AS).append(KEY_COLUMN)
                  .append(SEPARATOR).append(targetColumns)
                  .append(FROM).append(baseRelation.getAssociativeTable())
                  .append(WHERE_).append(baseRelation.getFk1());

        // build nested query for relations
        List<Relation> relations = domainPathDetail.getRelations();
        if (!baseRelation.getFk2().equals("id") && relations.isEmpty()) {// for many-to-one
            relations.add(new Relation(baseRelation.getFk1(), mainTableName, baseRelation.getFk2()));
        }

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
