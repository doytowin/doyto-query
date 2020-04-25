package win.doyto.query.web.response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ErrorCode
 *
 * @author f0rb on 2017-03-05.
 */
public interface ErrorCode {

    Integer getCode();

    String getMessage();

    default boolean isSuccess() {
        return Integer.valueOf(0).equals(getCode());
    }

    static <D> JsonResponse<D> build(D data) {
        return new JsonResponse<D>().setData(data);
    }

    static ErrorCode build(ErrorCode errorCode) {
        return build(errorCode.getCode(), errorCode.getMessage());
    }

    static ErrorCode build(String message) {
        return build(-1, message);
    }

    static ErrorCode build(Integer code, String message) {
        return new JsonResponse<>().setCode(code).setMessage(message);
    }

    static void assertNotNull(Object target, ErrorCode errorCode, Object... messages) {
        assertFalse(target == null, errorCode, messages);
    }

    static void assertTrue(boolean condition, ErrorCode errorCode, Object... messages) {
        assertFalse(!condition, errorCode, messages);
    }

    static void assertFalse(boolean condition, ErrorCode errorCode, Object... messages) {
        if (condition) {
            fail(errorCode, messages);
        }
    }

    static void fail(ErrorCode errorCode, Object... messages) {
        Logger logger = LoggerFactory.getLogger(ErrorCode.class);
        if (logger.isWarnEnabled()) {
            logger.warn("[{}]{} {}", errorCode.getCode(), errorCode.getMessage(), StringUtils.join(messages, ", "));
        }
        throw new ErrorCodeException(errorCode);
    }
}
