package win.doyto.query.web.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ErrorCodeTest
 *
 * @author f0rb on 2020-04-01
 */
class ErrorCodeTest {

    @Test
    void testAssertTrue() {
        ErrorCode.assertTrue(true, ErrorCode.build("fail"));

        try {
            ErrorCode.assertTrue(false, ErrorCode.build("fail"));
            fail();
        } catch (ErrorCodeException e) {
            assertFalse(e.getErrorCode().isSuccess());
            assertEquals(-1, ErrorCode.build(e.getErrorCode()).getCode());
        }
    }

    @Test
    void assertNotNull() {
        ErrorCode.assertNotNull("ok", ErrorCode.build("fail"));

        try {
            ErrorCode.assertNotNull(null, ErrorCode.build("fail"));
            fail();
        } catch (ErrorCodeException e) {
            assertFalse(e.getErrorCode().isSuccess());
        }
    }
}