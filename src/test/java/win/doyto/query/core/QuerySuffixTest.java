package win.doyto.query.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}