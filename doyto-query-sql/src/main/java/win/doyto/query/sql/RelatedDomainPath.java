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

import java.lang.reflect.Field;

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
        String targetDomainTable = domainPathDetail.getTargetTable();
        // select columns from target domain `joinTables[n]`
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SELECT).append(PLACE_HOLDER).append(AS).append(KEY_COLUMN)
                  .append(SEPARATOR).append(targetColumns)
                  .append(FROM).append(targetDomainTable)
                  .append(WHERE_).append(domainPathDetail.getForeignFieldColumn());
        // nested query for medium domains
        sqlBuilder.append(buildNestedQuery());
        return sqlBuilder;
    }

    public String buildNestedQuery() {
        String[] joinIds = domainPathDetail.getJoinIds();
        String[] joinTables = domainPathDetail.getJoinTables();
        if (!domainPathDetail.getLocalFieldColumn().equals("id")) {// for many-to-one
            joinIds = new String[]{domainPathDetail.getForeignFieldColumn(), joinIds[0]};
            joinTables = new String[]{mainTableName};
        }

        int n = joinIds.length - 1;
        StringBuilder sqlBuilder = new StringBuilder();
        for (int i = n - 1; i >= 0; i--) {
            sqlBuilder.append(IN).append(OP).append(LF).append(SPACE + SPACE)
                       .append(SELECT).append(joinIds[i + 1]).append(FROM).append(joinTables[i])
                       .append(WHERE).append(joinIds[i]);
        }

        sqlBuilder.append(EQUAL_HOLDER);
        if (n > 0) sqlBuilder.append(LF);
        sqlBuilder.append(StringUtils.repeat(CP, Math.max(0, n)));
        return sqlBuilder.toString();
    }

    public static String build(String[] joinIds, String[] joinTables) {
        StringBuilder sqlBuilder = new StringBuilder();
        int n = joinIds.length - 1;
        for (int i = n - 1; i >= 0; i--) {
            sqlBuilder.append(IN).append(OP)
                      .append(SELECT).append(joinIds[i + 1]).append(FROM).append(joinTables[i])
                      .append(WHERE).append(joinIds[i]);
        }
        return sqlBuilder.toString();
    }

}
