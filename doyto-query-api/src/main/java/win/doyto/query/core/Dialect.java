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

package win.doyto.query.core;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Dialect
 *
 * @author f0rb on 2019-06-02
 * @since 0.1.2
 */
public interface Dialect {

    /**
     * build page SQL for different
     *
     * @param sql    SQL will always be SELECT clause.
     * @param limit  LIMIT number which is GTE 0.
     * @param offset OFFSET number which is GTE 0.
     * @return pageable sql
     */
    String buildPageSql(String sql, int limit, long offset);

    default String wrapLabel(String fieldName) {
        return fieldName;
    }

    default String buildInsertIgnore(StringBuilder insertBuilder, String tableName, String k1, String k2) {
        return insertBuilder.insert(insertBuilder.indexOf("INTO"), "IGNORE ").toString();
    }

    default void buildInsertUpdate(StringBuilder insertSqlBuilder, String[] toUpdateColumns) {
        insertSqlBuilder.append(" ON DUPLICATE KEY UPDATE ");
        String update = Arrays.stream(toUpdateColumns)
                              .map(column -> column + " = VALUES (" + column + ")")
                              .collect(Collectors.joining(", "));
        insertSqlBuilder.append(update);
    }

    default String forShare() {
        return " FOR SHARE";
    }

    default String forUpdate() {
        return " FOR UPDATE";
    }

    default <I> I resolveKey(Number key, Class<I> idClass) {
        if (idClass.isAssignableFrom(Long.class)) {
            return idClass.cast(key.longValue());
        }
        return idClass.cast(key.intValue());
    }

    default String wrapSelectForUpdate(String sql, String column) {
        return sql;
    }

    default boolean supportMultiGeneratedKeys() {
        return false;
    }

    default String resolveKeyColumn(String idColumn) {
        return idColumn;
    }

    default String alterBatchInsert(String given) {
        return given;
    }
    default String convertMultiColumnsIn(StringBuilder sqlBuilder, String k1Column, String k2Column, int size) {
        return sqlBuilder.toString();
    }
}
