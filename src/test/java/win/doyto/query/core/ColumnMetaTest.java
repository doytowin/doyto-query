package win.doyto.query.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ColumnMetaTest
 *
 * @author f0rb on 2020-01-24
 */
class ColumnMetaTest {

    @Test
    void supportOr() {
        ColumnMeta columnMeta = new ColumnMeta("usernameOrEmailOrMobile", "test", new ArrayList<>());
        String andSql = columnMeta.defaultSql(QuerySuffix.Like, "?");
        assertEquals("(username LIKE ? OR email LIKE ? OR mobile LIKE ?)", andSql);
    }

}