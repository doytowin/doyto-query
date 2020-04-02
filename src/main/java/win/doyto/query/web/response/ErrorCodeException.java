package win.doyto.query.web.response;

import lombok.Getter;

/**
 * BusinessException
 *
 * @author f0rb on 2017-03-19.
 */
public class ErrorCodeException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;

    public ErrorCodeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
