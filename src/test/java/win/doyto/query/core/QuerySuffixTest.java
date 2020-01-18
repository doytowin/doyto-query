package win.doyto.query.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * QuerySuffixTest
 *
 * @author f0rb on 2020-01-17
 */
class QuerySuffixTest {

    @Test
    void getEx() {
        assertEquals("(null)", QuerySuffix.Ex.COLLECTION.getEx(Arrays.asList()));
        assertEquals("(?)", QuerySuffix.Ex.COLLECTION.getEx(Arrays.asList(1)));
        assertEquals("(?, ?)", QuerySuffix.Ex.COLLECTION.getEx(Arrays.asList(1, 2)));
    }
}