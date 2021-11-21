package win.doyto.query.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.test.TestEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ColumnUtilTest
 *
 * @author f0rb on 2021-11-21
 */
class ColumnUtilTest {

    @Test
    void testIsSingleColumn() {
        assertTrue(ColumnUtil.isSingleColumn("col"));
        assertFalse(ColumnUtil.isSingleColumn("col1, col2"));
        assertFalse(ColumnUtil.isSingleColumn("col1", "col2", "col3"));
    }

    @Test
    void resolveSelectColumns() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);
        String columns = StringUtils.join(ColumnUtil.resolveSelectColumns(TestEntity.class), ", ");
        assertEquals("username, password, mobile, email, nickname, user_level AS userLevel, memo, valid, id", columns);
    }
}