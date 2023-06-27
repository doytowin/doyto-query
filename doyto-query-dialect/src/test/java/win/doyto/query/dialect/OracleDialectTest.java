/*
 * Copyright © 2019-2023 Forb Yuan
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
 * OracleDialectTest
 *
 * @author f0rb on 2023/6/26
 * @since 1.0.2
 */
class OracleDialectTest {
    OracleDialect dialect = new OracleDialect();

    @Test
    void buildPageSql() {
        String pageSql = dialect.buildPageSql("SELECT username, password FROM user WHERE valid = true", 10, 100);
        String expected = "SELECT username, password FROM " +
                "(SELECT ROWNUM rn, t1.* FROM (" +
                "SELECT * FROM user WHERE valid = true" +
                ") t1 WHERE ROWNUM <= 110 ) t2 WHERE t2.rn > 100";
        assertEquals(expected, pageSql);
    }

    @Test
    void alterBatchInsert() {
        String given = "INSERT INTO t_role (role_name, role_code, create_user_id) " +
                "VALUES (?, ?, ?), (?, ?, ?), (?, ?, ?)";
        String actual = dialect.alterBatchInsert(given);
        String expected = "INSERT INTO t_role (role_name, role_code, create_user_id) " +
                "SELECT ?, ?, ? FROM DUAL " +
                "UNION ALL SELECT ?, ?, ? FROM DUAL " +
                "UNION ALL SELECT ?, ?, ? FROM DUAL";
        assertEquals(expected, actual);
    }

    @Test
    void buildInsertIgnore() {
        String given = "INSERT INTO a_user_and_role (user_id, role_id, create_user_id)" +
                " VALUES (?, ?, 0), (?, ?, 0)";
        String actual = dialect.buildInsertIgnore(new StringBuilder(given), "a_user_and_role", "user_id", "role_id");
        String expected = "INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(a_user_and_role(user_id,role_id)) */ " +
                "INTO a_user_and_role (user_id, role_id, create_user_id)" +
                " SELECT ?, ?, 0 FROM DUAL UNION ALL SELECT ?, ?, 0 FROM DUAL";
        assertEquals(expected, actual);
    }
}