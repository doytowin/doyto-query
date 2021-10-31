package win.doyto.query.web.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.ErrorCodeException;

/**
 * CommonExceptionHandler
 *
 * @author f0rb on 2021-10-31
 */
@Slf4j
@ControllerAdvice
@ResponseBody
class CommonExceptionHandler {

    @ExceptionHandler(ErrorCodeException.class)
    public ErrorCode errorCodeException(ErrorCodeException e) {
        log.warn("ErrorCodeException: {}", e.getMessage());
        return e.getErrorCode();
    }

}
