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

package win.doyto.query.dialect;

import win.doyto.query.core.Dialect;

/**
 * SQLiteDialect
 *
 * @author f0rb on 2023/6/25
 * @since 1.0.2
 */
public class SQLiteDialect implements Dialect {
    @Override
    public String buildPageSql(String sql, int limit, long offset) {
        return sql + " LIMIT " + limit + " OFFSET " + offset;
    }

    @Override
    public String buildInsertIgnore(StringBuilder insertBuilder, String tableName, String k1, String k2) {
        return insertBuilder.insert(insertBuilder.indexOf("INTO"), "OR IGNORE ").toString();
    }

    @Override
    public String resolveKeyColumn(String idColumn) {
        return "last_insert_rowid()";
    }
}
