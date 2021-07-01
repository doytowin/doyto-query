package win.doyto.query.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import win.doyto.query.core.test.TestEnum;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * QuerySuffixTest
 *
 * @author f0rb on 2020-01-17
 */
class QuerySuffixTest {

    @ParameterizedTest
    @CsvSource({
            "id, 1, id = ?, 1",
            "idNot, 2, id != ?, 2",
            "testLikeEq, test, testLike = ?, test",
            "nameLike, test, name LIKE ?, %test%",
            "nameStart, test, name LIKE ?, test%",
    })
    void testClauseAndValueGeneration(String fieldName, String value, String expectedSql, String expectedValue) {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = QuerySuffix.buildConditionForField(fieldName, argList, value);
        assertThat(andSql).isEqualTo(expectedSql);
        assertThat(argList).containsExactly(expectedValue);
    }

    @Test
    void getPlaceHolderEx() {
        QuerySuffix.ValueProcessor inValueProcessor = new QuerySuffix.InValueProcessor();
        assertEquals("(null)", inValueProcessor.getPlaceHolderEx(Arrays.asList()));
        assertEquals("(?)", inValueProcessor.getPlaceHolderEx(Arrays.asList(1)));
        assertEquals("(?, ?)", inValueProcessor.getPlaceHolderEx(Arrays.asList(1, 2)));
    }

    @Test
    void buildConditionForFieldContainsOr() {
        ArrayList<Object> argList = new ArrayList<>();
        String condition = QuerySuffix.buildConditionForFieldContainsOr("usernameOrUserCodeLike", argList, "test");
        assertEquals("(username = ? OR userCode LIKE ?)", condition);
        assertThat(argList).containsExactly("test", "%test%");
    }

    @Test
    void buildConditionForFieldContainsOrAndAlias() {
        ArrayList<Object> argList = new ArrayList<>();
        String condition = QuerySuffix.buildConditionForFieldContainsOr("u.usernameOrUserCodeLike", argList, "test");
        assertEquals("(u.username = ? OR u.userCode LIKE ?)", condition);
        assertThat(argList).containsExactly("test", "%test%");
    }

    @Test
    void buildLikeWithBlankValue() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = QuerySuffix.buildConditionForField("nameLike", argList, " ");
        assertThat(andSql).isNull();
        assertThat(argList).isEmpty();
    }

    @Test
    void buildIn() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = QuerySuffix.buildConditionForField("idIn", argList, Arrays.asList(1, 3, 5));
        assertThat(andSql).isEqualTo("id IN (?, ?, ?)");
        assertThat(argList).containsExactly(1, 3, 5);
    }

    @Test
    void buildInEnums() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = QuerySuffix.buildConditionForField("stateIn", argList, Arrays.asList(TestEnum.VIP, TestEnum.NORMAL));
        assertThat(andSql).isEqualTo("state IN (?, ?)");
        assertThat(argList).containsExactly(0, 1);
    }

    @Test
    void buildInNull() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = QuerySuffix.buildConditionForField("stateIn", argList, Arrays.asList());
        assertThat(andSql).isEqualTo("state IN (null)");
        assertThat(argList).isEmpty();
    }

    @Test
    void buildNotInNull() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = QuerySuffix.buildConditionForField("stateNotIn", argList, Arrays.asList());
        assertThat(andSql).isNull();
        assertThat(argList).containsExactly();
    }

    @Test
    void shouldIgnoreNonCollectionValueForNotIn() {
        assertTrue(QuerySuffix.NotIn.shouldIgnore(""));
        assertTrue(QuerySuffix.NotIn.shouldIgnore(1));
    }

    @Test
    void shouldIgnoreBlankValueForLike() {
        assertTrue(QuerySuffix.Like.shouldIgnore("  "));
    }

    @Test
    void shouldIgnoreNonStringValueForLike() {
        assertTrue(QuerySuffix.Like.shouldIgnore(1));
    }
}