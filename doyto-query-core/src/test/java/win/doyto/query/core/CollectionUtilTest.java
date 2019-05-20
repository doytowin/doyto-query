package win.doyto.query.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * CollectionUtilTest
 *
 * @author f0rb
 */
class CollectionUtilTest {

    @Test
    void first() {
        assertNull(CollectionUtil.first(new LinkedList<>()));
        assertEquals("hello", CollectionUtil.first(Arrays.asList("hello")));
        assertEquals("hello", CollectionUtil.first(Arrays.asList("hello", "world")));
    }
}