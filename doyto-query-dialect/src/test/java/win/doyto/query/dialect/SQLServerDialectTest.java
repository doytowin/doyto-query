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
 * SQLServerDialectTest
 *
 * @author f0rb on 2023/6/23
 * @since 1.0.2
 */
class SQLServerDialectTest {

    private SQLServerDialect sqlServerDialect;

    @Test
    void buildPageSqlForSelectWithoutOrderBy() {
        sqlServerDialect = new SQLServerDialect();
        String pageSql = sqlServerDialect.buildPageSql("SELECT username, password FROM user WHERE valid = true", 10, 100);
        assertEquals("SELECT username, password FROM user WHERE valid = true ORDER BY id offset 100 row fetch next 10 row only", pageSql);
    }
    
    @Test
    void buildPageSqlForSelectWithOrderBy() {
        sqlServerDialect = new SQLServerDialect();
        String pageSql = sqlServerDialect.buildPageSql("SELECT username, password FROM user WHERE valid = true ORDER BY username DESC", 10, 100);
        assertEquals("SELECT username, password FROM user WHERE valid = true ORDER BY username DESC offset 100 row fetch next 10 row only", pageSql);
    }
    
}