package win.doyto.query.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * ColumnMetaTest
 *
 * @author f0rb
 * @date 2019-05-17
 */
class ColumnMetaTest {

    @Test
    void splitByOr() {
        assertArrayEquals(new String[] {"user", "Email", "Order"},
                          ColumnMeta.splitByOr("userOrEmailOrOrder"));
    }
}