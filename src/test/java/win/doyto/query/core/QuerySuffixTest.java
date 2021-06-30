package win.doyto.query.core;

import org.junit.jupiter.api.Test;
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

    @Test
    void getPlaceHolderEx() {
        assertEquals("(null)", QuerySuffix.ValueProcessor.COLLECTION.getPlaceHolderEx(Arrays.asList()));
        assertEquals("(?)", QuerySuffix.ValueProcessor.COLLECTION.getPlaceHolderEx(Arrays.asList(1)));
        assertEquals("(?, ?)", QuerySuffix.ValueProcessor.COLLECTION.getPlaceHolderEx(Arrays.asList(1, 2)));
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
    void buildEq() {
        ArrayList<Object> argList = new ArrayList<>();
        String condition = QuerySuffix.buildConditionForField("testLikeEq", argList, "test");
        assertThat(condition).isEqualTo("testLike = ?");
        assertThat(argList).containsExactly("test");
    }

    @Test
    void buildLike() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = QuerySuffix.buildConditionForField("nameLike", argList, "test");
        assertThat(andSql).isEqualTo("name LIKE ?");
        assertThat(argList).containsExactly("%test%");
    }

    @Test
    void buildStart() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = QuerySuffix.buildConditionForField("nameStart", argList, "test");
        assertThat(andSql).isEqualTo("name LIKE ?");
        assertThat(argList).containsExactly("test%");
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
    void buildIInNull() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = QuerySuffix.buildConditionForField("stateIn", argList, Arrays.asList());
        assertThat(andSql).isEqualTo("state IN (null)");
        assertThat(argList).isEmpty();
    }

    @Test
    void buildINotInNull() {
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
}