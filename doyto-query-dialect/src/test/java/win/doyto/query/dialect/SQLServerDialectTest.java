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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * SQLServerDialectTest
 *
 * @author f0rb on 2023/6/23
 * @since 1.0.2
 */
class SQLServerDialectTest {

    private final SQLServerDialect dialect = new SQLServerDialect();

    @Test
    void buildPageSqlForSelectWithoutOrderBy() {
        String pageSql = dialect.buildPageSql("SELECT username, password FROM user WHERE valid = true", 10, 100);
        assertEquals("SELECT username, password FROM user WHERE valid = true ORDER BY id offset 100 row fetch next 10 row only", pageSql);
    }

    @Test
    void buildPageSqlForSelectWithOrderBy() {
        String pageSql = dialect.buildPageSql("SELECT username, password FROM user WHERE valid = true ORDER BY username DESC", 10, 100);
        assertEquals("SELECT username, password FROM user WHERE valid = true ORDER BY username DESC offset 100 row fetch next 10 row only", pageSql);
    }

    @Test
    void buildPageSqlForSelectWithGroupBy() {
        String pageSql = dialect.buildPageSql("SELECT user_level AS userLevel, valid, count(*) AS count FROM t_user " +
                "WHERE valid = ? GROUP BY user_level, valid HAVING count(*) > ? AND count(*) < ?", 10, 20);
        assertEquals("SELECT user_level AS userLevel, valid, count(*) AS count FROM t_user " +
                "WHERE valid = ? GROUP BY user_level, valid HAVING count(*) > ? AND count(*) < ? " +
                "ORDER BY user_level, valid offset 20 row fetch next 10 row only", pageSql);
    }

    @Test
    void resolveKeyColumn() {
        assertEquals("GENERATED_KEYS", dialect.resolveKeyColumn("id"));
    }

    @Test
    void convertMultiColumnsIn() {

        StringBuilder sqlBuilder = new StringBuilder("SELECT count(*) FROM a_user_and_role WHERE (user_id, role_id) IN ((?, ?), (?, ?))");
        assertEquals("SELECT count(*) FROM a_user_and_role WHERE user_id = ? AND role_id = ? OR user_id = ? AND role_id = ?",
                dialect.convertMultiColumnsIn(sqlBuilder, "user_id", "role_id", 2));
    }
}