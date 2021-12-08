package win.doyto.query.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static win.doyto.query.util.CommonUtil.*;

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
        assertArrayEquals(new String[] {"user", "emailAddress", "order"}, splitByOr("userOrEmailAddressOrOrder"));
    }

    @Test
    void testReplaceHolderInString() {
        assertEquals("_test1_", replaceHolderInString(new PlaceHolderObject("test1"), "_${part1}_"));
    }

    @Test
    void replaceHolderInStringShouldReadGetterFirst() {
        assertEquals("_test1_test2_", replaceHolderInString(new PlaceHolderObject("test1"), "_${part1}_${part2}_"));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class PlaceHolderObject {
        private String part1;

        public String getPart2() {
            return "test2";
        }
    }
}