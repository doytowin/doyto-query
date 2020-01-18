package win.doyto.query.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * OrderByBuilderTest
 *
 * @author f0rb on 2020-01-01
 */
class OrderByBuilderTest {
    @Test
    void build() {
        assertEquals("valid,asc;id,desc", OrderByBuilder.create().asc("valid").desc("id").toString());
        assertEquals("field(gender,'male','female');id,desc",
                     OrderByBuilder.create().field("gender", "'male'", "'female'").desc("id").toString());
    }
}