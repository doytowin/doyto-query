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
 * OracleDialect
 *
 * @author f0rb on 2023/6/26
 * @since 1.0.2
 */
public class OracleDialect implements Dialect {
    @Override
    public String buildPageSql(String sql, int limit, long offset) {
        if (sql.contains("GROUP BY")) {
            return  "SELECT * FROM " +
                    "(SELECT ROWNUM rn, ora1.* FROM (" +
                    sql +
                    ") ora1 WHERE ROWNUM <= " +
                    (offset + limit) +
                    " ) ora2 WHERE ora2.rn > " + offset;
        }
        int fromIndex = sql.indexOf("FROM");
        String select = sql.substring(0, fromIndex);
        sql = "SELECT * " + sql.substring(fromIndex);
        return select + "FROM " +
                "(SELECT ROWNUM rn, ora1.* FROM (" +
                sql +
                ") ora1 WHERE ROWNUM <= " +
                (offset + limit) +
                " ) ora2 WHERE ora2.rn > " + offset;
    }

    @Override
    public String alterBatchInsert(String given) {
        return given.concat(",").replace("VALUES (", "SELECT ")
                .replace("),", " FROM DUAL")
                .replace("DUAL (", "DUAL UNION ALL SELECT ");
    }

    @Override
    public String buildInsertIgnore(StringBuilder insertBuilder, String tableName, String k1, String k2) {
        String ignore = "/*+ IGNORE_ROW_ON_DUPKEY_INDEX(" + tableName + "(" + k1 + "," + k2 + ")) */ ";
        return alterBatchInsert(insertBuilder.insert(insertBuilder.indexOf("INTO"), ignore).toString());
    }
}
