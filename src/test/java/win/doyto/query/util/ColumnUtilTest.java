package win.doyto.query.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ColumnUtilTest
 *
 * @author f0rb
 */
class ColumnUtilTest {

    @Test
    void escapeLike() {
        assertNull(ColumnUtil.escapeLike(null));
        assertEquals("", ColumnUtil.escapeLike(""));

        assertEquals("%f0rb%", ColumnUtil.escapeLike("f0rb"));
        assertNotEquals("%%%", ColumnUtil.escapeLike("%"));
        assertEquals("%\\%%", ColumnUtil.escapeLike("%"));
        assertEquals("%f0rb\\%%", ColumnUtil.escapeLike("f0rb%"));

        assertNotEquals("%_%", ColumnUtil.escapeLike("_"));
        assertEquals("%\\_%", ColumnUtil.escapeLike("_"));

    }
}