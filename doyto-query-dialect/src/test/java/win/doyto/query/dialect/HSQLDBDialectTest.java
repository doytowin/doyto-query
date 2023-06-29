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
 * HSQLDBDialectTest
 *
 * @author f0rb on 2021-06-12
 */
class HSQLDBDialectTest {

    private HSQLDBDialect hsqldbDialect = new HSQLDBDialect();

    @Test
    void buildPageSqlForSelect() {
        String pageSql = hsqldbDialect.buildPageSql("SELECT username, password FROM user WHERE valid = true", 10, 100);
        assertEquals("SELECT username, password FROM user WHERE valid = true LIMIT 10 OFFSET 100", pageSql);
    }

    @Test
    void buildPageSqlForDelete() {
        String pageSql = hsqldbDialect.buildPageSql("DELETE FROM user WHERE valid = true", 10, 100);
        assertEquals("DELETE FROM user WHERE valid = true LIMIT 10", pageSql);
    }
}