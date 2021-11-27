package win.doyto.query.web.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * PresetErrorCodeTest
 *
 * @author f0rb on 2020-04-12
 */
class PresetErrorCodeTest {

    @Test
    void count() {
        assertEquals(0, PresetErrorCode.SUCCESS.getCode().intValue());
        assertEquals(9, PresetErrorCode.ENTITY_NOT_FOUND.getCode().intValue());
    }

}