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
    void buildAndSql() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = QuerySuffix.buildAndSql(argList, "test", "usernameOrUserCodeLike");
        assertEquals("(username = ? OR userCode LIKE ?)", andSql);
        assertThat(argList).containsExactly("test", "%test%");
    }

    @Test
    void buildAndSqlWithAlias() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = QuerySuffix.buildAndSql(argList, "test", "u.usernameOrUserCodeLike");
        assertEquals("(u.username = ? OR u.userCode LIKE ?)", andSql);
        assertThat(argList).containsExactly("test", "%test%");
    }

    @Test
    void buildEq() {
        ArrayList<Object> argList = new ArrayList<>();
        String andSql = QuerySuffix.buildAndSql(argList, "test", "testLikeEq");
        assertEquals("testLike = ?", andSql);
        assertThat(argList).containsExactly("test");
    }
}