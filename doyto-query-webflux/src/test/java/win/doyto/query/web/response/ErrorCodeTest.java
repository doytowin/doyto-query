package win.doyto.query.web.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ErrorCodeTest
 *
 * @author f0rb on 2020-04-01
 */
class ErrorCodeTest {

    private ErrorCode failCode = ErrorCode.build("fail");

    @Test
    void testAssertTrue() {
        ErrorCode.assertTrue(true, failCode);

        try {
            ErrorCode.assertTrue(false, failCode);
            fail();
        } catch (ErrorCodeException e) {
            assertFalse(e.getErrorCode().isSuccess());
            assertEquals(-1, ErrorCode.build(e.getErrorCode()).getCode());
        }
    }

    @Test
    void assertNotNull() {
        ErrorCode.assertNotNull("ok", failCode);

        try {
            ErrorCode.assertNotNull(null, failCode);
            fail();
        } catch (ErrorCodeException e) {
            assertFalse(e.getErrorCode().isSuccess());
        }
    }
}