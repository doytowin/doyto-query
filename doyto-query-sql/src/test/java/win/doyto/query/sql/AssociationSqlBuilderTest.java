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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AssociationSqlBuilderTest
 *
 * @author f0rb on 2021-12-17
 */
class AssociationSqlBuilderTest {

    private AssociationSqlBuilder associationSqlBuilder;

    @BeforeEach
    void setUp() {
        String tableName = "t_user_and_role";
        String k1Column = "user_id";
        String k2Column = "role_id";
        associationSqlBuilder = new AssociationSqlBuilder(tableName, k1Column, k2Column);
    }

    @Test
    void testSelectK1ColumnByK2Id() {
        assertEquals("SELECT user_id FROM t_user_and_role WHERE role_id = ?",
                     associationSqlBuilder.getSelectK1ColumnByK2Id());
    }

    @Test
    void testSelectK2ColumnByK1Id() {
        assertEquals("SELECT role_id FROM t_user_and_role WHERE user_id = ?",
                     associationSqlBuilder.getSelectK2ColumnByK1Id());
    }

    @Test
    void testDeleteByK1() {
        assertEquals("DELETE FROM t_user_and_role WHERE user_id = ?",
                     associationSqlBuilder.getDeleteByK1());
    }

    @Test
    void testDeleteByK2() {
        assertEquals("DELETE FROM t_user_and_role WHERE role_id = ?",
                     associationSqlBuilder.getDeleteByK2());
    }

    @Test
    void testInsert() {
        List<UniqueKey<?, ?>> keys = Arrays.asList(new UniqueKey<>(1, 1), new UniqueKey<>(2, 3));
        SqlAndArgs sqlAndArgs = associationSqlBuilder.buildInsert(keys);
        assertThat(sqlAndArgs.getSql())
                .isEqualTo("INSERT INTO t_user_and_role (user_id, role_id) VALUES (?, ?), (?, ?)");
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 2, 3);
    }
}