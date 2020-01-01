package win.doyto.query.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * OrderByTest
 *
 * @author f0rb on 2020-01-01
 */
class OrderByTest {
    @Test
    void build() {
        assertEquals("valid,ASC;id,DESC", OrderBy.create().asc("valid").desc("id").toString());
        assertEquals("FIELD(gender,'male','female');id,DESC",
                     OrderBy.create().field("gender,'male','female'").desc("id").toString());
    }
}