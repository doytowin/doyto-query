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

package win.doyto.query.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.UniqueKey;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AssociationSqlBuilderTest
 *
 * @author f0rb on 2021-12-17
 */
class AssociationSqlBuilderTest {

    private AssociationSqlBuilder<Integer, Integer> associationSqlBuilder;

    @BeforeEach
    void setUp() {
        associationSqlBuilder = new AssociationSqlBuilder<>("user", "role");
    }

    @Test
    void testSelectK1ColumnByK2Id() {
        assertEquals("SELECT user_id FROM a_user_and_role WHERE role_id = ?",
                     associationSqlBuilder.getSelectK1ColumnByK2Id());
    }

    @Test
    void testSelectK2ColumnByK1Id() {
        assertEquals("SELECT role_id FROM a_user_and_role WHERE user_id = ?",
                     associationSqlBuilder.getSelectK2ColumnByK1Id());
    }

    @Test
    void testDeleteByK1() {
        assertEquals("DELETE FROM a_user_and_role WHERE user_id = ?",
                     associationSqlBuilder.getDeleteByK1());
    }

    @Test
    void testDeleteByK2() {
        assertEquals("DELETE FROM a_user_and_role WHERE role_id = ?",
                     associationSqlBuilder.getDeleteByK2());
    }

    private Set<UniqueKey<Integer, Integer>> testKeys() {
        return new LinkedHashSet<>(Arrays.asList(new UniqueKey<>(1, 1), new UniqueKey<>(2, 3)));
    }

    @Test
    void testInsert() {
        SqlAndArgs sqlAndArgs = associationSqlBuilder.buildInsert(testKeys());
        assertThat(sqlAndArgs.getSql())
                .isEqualTo("INSERT IGNORE INTO a_user_and_role (user_id, role_id) VALUES (?, ?), (?, ?)");
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 2, 3);
    }

    @Test
    void testDelete() {
        SqlAndArgs sqlAndArgs = associationSqlBuilder.buildDelete(testKeys());
        assertThat(sqlAndArgs.getSql())
                .isEqualTo("DELETE FROM a_user_and_role WHERE (user_id, role_id) IN ((?, ?), (?, ?))");
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 2, 3);
    }

    @Test
    void testCount() {
        SqlAndArgs sqlAndArgs = associationSqlBuilder.buildCount(testKeys());
        assertThat(sqlAndArgs.getSql())
                .isEqualTo("SELECT count(*) FROM a_user_and_role WHERE (user_id, role_id) IN ((?, ?), (?, ?))");
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 2, 3);
    }

    @Test
    void testInsertWithUser() {
        associationSqlBuilder.withCreateUserColumn("create_user_id");
        SqlAndArgs sqlAndArgs = associationSqlBuilder.buildInsert(testKeys(), 1);
        assertThat(sqlAndArgs.getSql())
                .isEqualTo("INSERT IGNORE INTO a_user_and_role (user_id, role_id, create_user_id) VALUES (?, ?, 1), (?, ?, 1)");
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 2, 3);
    }
}