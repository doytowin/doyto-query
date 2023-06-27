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

class SQLiteDialectTest {
    SQLiteDialect dialect = new SQLiteDialect();

    @Test
    void buildInsertIgnore() {
        String given = "INSERT INTO a_user_and_role (user_id, role_id, create_user_id)" +
                " VALUES (?, ?, 0), (?, ?, 0)";
        String actual = dialect.buildInsertIgnore(new StringBuilder(given), "a_user_and_role", "user_id", "role_id");
        String expected = "INSERT OR IGNORE INTO a_user_and_role (user_id, role_id, create_user_id)" +
                " VALUES (?, ?, 0), (?, ?, 0)";
        assertEquals(expected, actual);
    }
}