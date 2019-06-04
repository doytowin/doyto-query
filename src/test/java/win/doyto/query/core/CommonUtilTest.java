package win.doyto.query.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static win.doyto.query.core.CommonUtil.escapeLike;
import static win.doyto.query.core.CommonUtil.splitByOr;

/**
 * CommonUtilTest
 *
 * @author f0rb on 2019-05-31
 */
class CommonUtilTest {

    @Test
    void testEscapeLike() {
        assertNull(escapeLike(null));
        assertEquals("", escapeLike(""));

        assertEquals("%f0rb%", escapeLike("f0rb"));
        assertNotEquals("%%%", escapeLike("%"));
        assertEquals("%\\%%", escapeLike("%"));
        assertEquals("%f0rb\\%%", escapeLike("f0rb%"));

        assertNotEquals("%_%", escapeLike("_"));
        assertEquals("%\\_%", escapeLike("_"));
    }

    @Test
    void testSplitByOr() {
        assertArrayEquals(new String[] {"user", "Email", "Order"}, splitByOr("userOrEmailOrOrder"));
    }

}