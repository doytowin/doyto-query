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

package win.doyto.query.sql.field;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import win.doyto.query.test.TestEnum;
import win.doyto.query.test.TestQuery;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static win.doyto.query.sql.Constant.EMPTY;

/**
 * QuerySuffixTest
 *
 * @author f0rb on 2020-01-17
 */
class SqlQuerySuffixTest {
    ArrayList<Object> argList = new ArrayList<>();

    @SneakyThrows
    static Field fieldInTestQuery(String fieldName) {
        return TestQuery.class.getDeclaredField(fieldName);
    }

    static String buildConditionForField(String alias, Field field, List<Object> argList, Object value) {
        return new SuffixFieldProcessor(field, false).process(alias, argList, value);
    }

    @ParameterizedTest(name = "[{index}] {arguments}")
    @CsvSource(value = {
            "id, 1, id = ?, 1",
            "idNot, 2, id != ?, 2",
            "idNe, 2, id <> ?, 2",
            "testLikeEq, test, test_like = ?, test",
            "usernameLike, test, username LIKE ?, %test%",
            "usernameNotLike, test, username NOT LIKE ?, %test%",
            "usernameLike, %test%admin%, username LIKE ?, %test%admin%",
            "usernameNotLike, %test%admin%, username NOT LIKE ?, %test%admin%",
            "usernameContain, test%admin, username LIKE ? ESCAPE '\\', %test\\%admin%",
            "usernameNotContain, test, username NOT LIKE ?, %test%",
            "usernameStart, test, username LIKE ?, test%",
            "usernameNotStart, test, username NOT LIKE ?, test%",
            "usernameEnd, test, username LIKE ?, %test",
            "usernameNotEnd, test, username NOT LIKE ?, %test",
    })
    void testClauseAndValueGeneration(String fieldName, String value, String expectedSql, String expectedValue) {
        String andSql = buildConditionForField(EMPTY, fieldInTestQuery(fieldName), argList, value);
        assertThat(andSql).isEqualTo(expectedSql);
        assertThat(argList).containsExactly(expectedValue);
    }

    @Test
    void getPlaceHolderEx() {
        SqlQuerySuffix.ValueProcessor inValueProcessor = new SqlQuerySuffix.InValueProcessor();
        assertEquals("(null)", inValueProcessor.getPlaceHolderEx(emptyList()));
        assertEquals("(?)", inValueProcessor.getPlaceHolderEx(singletonList(1)));
        assertEquals("(?, ?)", inValueProcessor.getPlaceHolderEx(Arrays.asList(1, 2)));
    }

    @Test
    void buildLikeWithBlankValue() {
        String andSql = buildConditionForField(EMPTY, fieldInTestQuery("usernameLike"), argList, " ");
        assertThat(andSql).isNull();
        assertThat(argList).isEmpty();
    }

    @Test
    void buildIn() {
        String andSql = buildConditionForField(EMPTY, fieldInTestQuery("idIn"), argList, Arrays.asList(1, 3, 5));
        assertThat(andSql).isEqualTo("id IN (?, ?, ?)");
        assertThat(argList).containsExactly(1, 3, 5);
    }

    @Test
    void buildInEnums() {
        List<TestEnum> value = Arrays.asList(TestEnum.VIP, TestEnum.NORMAL);
        String andSql = buildConditionForField(EMPTY, fieldInTestQuery("userLevelIn"), argList, value);
        assertThat(andSql).isEqualTo("user_level IN (?, ?)");
        assertThat(argList).containsExactly(0, 1);
    }

    @Test
    void buildInNull() {
        String andSql = buildConditionForField(EMPTY, fieldInTestQuery("userLevelIn"), argList, emptyList());
        assertThat(andSql).isEqualTo("user_level IN (null)");
        assertThat(argList).isEmpty();
    }

    @Test
    void buildNotInNull() {
        String andSql = buildConditionForField(EMPTY, fieldInTestQuery("userLevelNotIn"), argList, emptyList());
        assertThat(andSql).isNull();
        assertThat(argList).isEmpty();
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

}