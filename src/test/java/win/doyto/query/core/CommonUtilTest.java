package win.doyto.query.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommonUtilTest
 *
 * @author f0rb on 2019-05-31
 */
class CommonUtilTest {

    @Test
    void escapeLike() {
        assertNull(CommonUtil.escapeLike(null));
        assertEquals("", CommonUtil.escapeLike(""));

        assertEquals("%f0rb%", CommonUtil.escapeLike("f0rb"));
        assertNotEquals("%%%", CommonUtil.escapeLike("%"));
        assertEquals("%\\%%", CommonUtil.escapeLike("%"));
        assertEquals("%f0rb\\%%", CommonUtil.escapeLike("f0rb%"));

        assertNotEquals("%_%", CommonUtil.escapeLike("_"));
        assertEquals("%\\_%", CommonUtil.escapeLike("_"));
    }

}