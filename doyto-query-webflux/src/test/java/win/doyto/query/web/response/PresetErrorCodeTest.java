package win.doyto.query.web.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * PresetErrorCodeTest
 *
 * @author f0rb on 2021-10-31
 */
class PresetErrorCodeTest {

    @Test
    void testErrorCodeIndex() {
        assertEquals(0, PresetErrorCode.SUCCESS.getCode());
        assertEquals(9, PresetErrorCode.ENTITY_NOT_FOUND.getCode());
    }

    @Test
    void testBuildErrorCodeWithArgs() {
        assertEquals("该接口不支持GET请求", PresetErrorCode.HTTP_METHOD_NOT_SUPPORTED.build("GET").getMessage());
    }

}