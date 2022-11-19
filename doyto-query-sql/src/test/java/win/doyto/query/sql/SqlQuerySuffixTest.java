/*
 * Copyright © 2019-2022 Forb Yuan
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
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import win.doyto.query.test.TestEnum;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ;

/**
 * QuerySuffixTest
 *
 * @author f0rb on 2020-01-17
 */
@ResourceLock(value = "mapCamelCaseToUnderscore", mode = READ)
class SqlQuerySuffixTest {

    @ParameterizedTest
    @CsvSource({
            "id, 1, id = ?, 1",
            "idNot, 2, id != ?, 2",
            "testLikeEq, test, test_like = ?, test",
            "nameNotLike, test, name NOT LIKE ?, %test%",
            "nameLike, test, name LIKE ?, %test%",
            "nameStart, test, name LIKE ?, test%",
    })
    void testClauseAndValueGeneration(String fieldName, String value, String expectedSql, String expectedValue) {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = SqlQuerySuffix.buildConditionForField(fieldName, argList, value);
        assertThat(andSql).isEqualTo(expectedSql);
        assertThat(argList).containsExactly(expectedValue);
    }

    @Test
    void getPlaceHolderEx() {
        SqlQuerySuffix.ValueProcessor inValueProcessor = new SqlQuerySuffix.InValueProcessor();
        assertEquals("(null)", inValueProcessor.getPlaceHolderEx(Arrays.asList()));
        assertEquals("(?)", inValueProcessor.getPlaceHolderEx(Arrays.asList(1)));
        assertEquals("(?, ?)", inValueProcessor.getPlaceHolderEx(Arrays.asList(1, 2)));
    }

    @Test
    void buildConditionForFieldContainsOr() {
        ArrayList<Object> argList = new ArrayList<>();
        String condition = SqlQuerySuffix.buildConditionForFieldContainsOr("usernameOrUserCodeLike", argList, "test");
        assertEquals("(username = ? OR user_code LIKE ?)", condition);
        assertThat(argList).containsExactly("test", "%test%");
    }

    @Test
    void buildConditionForFieldContainsOrAndAlias() {
        ArrayList<Object> argList = new ArrayList<>();
        String condition = SqlQuerySuffix.buildConditionForFieldContainsOr("u.usernameOrUserCodeLike", argList, "test");
        assertEquals("(u.username = ? OR u.user_code LIKE ?)", condition);
        assertThat(argList).containsExactly("test", "%test%");
    }

    @Test
    void buildLikeWithBlankValue() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = SqlQuerySuffix.buildConditionForField("nameLike", argList, " ");
        assertThat(andSql).isNull();
        assertThat(argList).isEmpty();
    }

    @Test
    void buildIn() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = SqlQuerySuffix.buildConditionForField("idIn", argList, Arrays.asList(1, 3, 5));
        assertThat(andSql).isEqualTo("id IN (?, ?, ?)");
        assertThat(argList).containsExactly(1, 3, 5);
    }

    @Test
    void buildInEnums() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = SqlQuerySuffix.buildConditionForField("stateIn", argList, Arrays.asList(TestEnum.VIP, TestEnum.NORMAL));
        assertThat(andSql).isEqualTo("state IN (?, ?)");
        assertThat(argList).containsExactly(0, 1);
    }

    @Test
    void buildInNull() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = SqlQuerySuffix.buildConditionForField("stateIn", argList, Arrays.asList());
        assertThat(andSql).isEqualTo("state IN (null)");
        assertThat(argList).isEmpty();
    }

    @Test
    void buildNotInNull() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = SqlQuerySuffix.buildConditionForField("stateNotIn", argList, Arrays.asList());
        assertThat(andSql).isNull();
        assertThat(argList).containsExactly();
    }

    @Test
    void shouldIgnoreNonCollectionValueForNotIn() {
        assertTrue(SqlQuerySuffix.NotIn.shouldIgnore(""));
        assertTrue(SqlQuerySuffix.NotIn.shouldIgnore(1));
    }

    @Test
    void shouldIgnoreBlankValueForLike() {
        assertTrue(SqlQuerySuffix.Like.shouldIgnore("  "));
    }

    @Test
    void shouldIgnoreNonStringValueForLike() {
        assertTrue(SqlQuerySuffix.Like.shouldIgnore(1));
    }

    @Test
    void testForNotLike() {
        assertTrue(SqlQuerySuffix.NotLike.shouldIgnore("  "));
        assertTrue(SqlQuerySuffix.NotLike.shouldIgnore(1));
    }

    @Test
    void buildEnd() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = SqlQuerySuffix.buildConditionForField("emailEnd", argList, "@doyto.win");
        assertThat(andSql).isEqualTo("email LIKE ?");
        assertThat(argList).containsExactly("%@doyto.win");
    }

}