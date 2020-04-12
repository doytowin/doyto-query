package win.doyto.query.web.component;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.ErrorCodeException;
import win.doyto.query.web.response.PresetErrorCode;

import java.util.List;

/**
 * CommonExceptionHandler
 *
 * @author f0rb on 2017-03-21.
 */
@Slf4j
@ControllerAdvice
@ResponseBody
class CommonExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorCode httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException: " + e.getMessage(), e.getCause());
        return PresetErrorCode.HTTP_METHOD_NOT_SUPPORTED.build(e.getMethod());
    }

    /**
     * 参数类型转换错误
     *
     * @param e 异常
     * @return result
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorCode methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException: " + e.getMessage(), e);
        return PresetErrorCode.ARGUMENT_TYPE_MISMATCH;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorCode httpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException: " + e.getMessage(), e);
        if (e.getCause() instanceof InvalidFormatException) {
            return PresetErrorCode.ARGUMENT_FORMAT_ERROR.build(((InvalidFormatException) e.getCause()).getValue());
        }
        return PresetErrorCode.REQUEST_BODY_ERROR;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ErrorCode uploadFileOverMaxSizeException(MaxUploadSizeExceededException e) {
        log.error("MaxUploadSizeExceededException: {}", e.getMessage());
        return PresetErrorCode.FILE_UPLOAD_OVER_MAX_SIZE;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorCode httpMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: " + e.getMessage(), e);
        return buildByBindingResult(e.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(BindException.class)
    public ErrorCode httpBindException(BindException e) {
        return buildByBindingResult(e.getBindingResult().getFieldErrors());
    }

    private ErrorCode buildByBindingResult(List<FieldError> fieldErrors) {
        ErrorCode errorCode = ErrorCode.build(PresetErrorCode.ARGUMENT_VALIDATION_FAILED);
        fieldErrors.forEach(fieldError -> errorCode.addError(fieldError.getField(), fieldError.getDefaultMessage()));
        return errorCode;
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ErrorCode duplicateKeyException(DuplicateKeyException e) {
        log.error("DuplicateKeyException: " + e.getMessage(), e);
        return PresetErrorCode.DUPLICATE_KEY_EXCEPTION;
    }

    @ExceptionHandler(Exception.class)
    public ErrorCode exception(Exception e) {
        log.error("Unknown Exception", e);
        return PresetErrorCode.ERROR;
    }

    @ExceptionHandler(ErrorCodeException.class)
    public ErrorCode handleErrorCodeException(ErrorCodeException e) {
        log.warn("ErrorCodeException: {}", e.getMessage());
        return e.getErrorCode();
    }

}
