/*
 * Copyright © 2019-2021 Forb Yuan
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AssociationSqlBuilderTest
 *
 * @author f0rb on 2021-12-17
 */
class AssociationSqlBuilderTest {

    @Test
    void testSelectK1ColumnByK2Id() {
        String tableName = "t_user_and_role";
        String k1Column = "user_id";
        String k2Column = "role_id";

        AssociationSqlBuilder associationSqlBuilder = new AssociationSqlBuilder(tableName, k1Column, k2Column);

        assertEquals("SELECT user_id FROM t_user_and_role WHERE role_id = ?", associationSqlBuilder.getSelectK1ColumnByK2Id());
    }
}