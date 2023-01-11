/*
 * Copyright © 2019-2023 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.web.component;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.ErrorCodeException;
import win.doyto.query.web.response.ErrorResponse;

import javax.validation.ConstraintViolationException;

import static win.doyto.query.web.response.PresetErrorCode.*;

/**
 * CommonExceptionHandler
 *
 * @author f0rb on 2017-03-21.
 */
@Slf4j
@ControllerAdvice
@ResponseBody
@AllArgsConstructor
class CommonExceptionHandler {

    private ErrorCodeI18nService errorCodeI18nService;

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorCode httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException: " + e.getMessage(), e.getCause());
        return errorCodeI18nService.buildErrorCode(HTTP_METHOD_NOT_SUPPORTED, e.getMethod());
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
        return errorCodeI18nService.buildErrorCode(ARGUMENT_TYPE_MISMATCH);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorCode httpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException: " + e.getMessage(), e);
        if (e.getCause() instanceof InvalidFormatException) {
            return errorCodeI18nService.buildErrorCode(ARGUMENT_FORMAT_ERROR, ((InvalidFormatException) e.getCause()).getValue());
        }
        return errorCodeI18nService.buildErrorCode(REQUEST_BODY_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ErrorCode maxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("MaxUploadSizeExceededException: {}", e.getMessage());
        return errorCodeI18nService.buildErrorCode(FILE_UPLOAD_OVER_MAX_SIZE);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorCode methodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage());
        return new ErrorResponse(errorCodeI18nService.buildErrorCode(ARGUMENT_VALIDATION_FAILED), e.getBindingResult());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorCode constraintViolationException(ConstraintViolationException e) {
        log.error("ConstraintViolationException: {}", e.getMessage());
        return new ErrorResponse(errorCodeI18nService.buildErrorCode(ARGUMENT_VALIDATION_FAILED), e.getConstraintViolations());
    }

    @ExceptionHandler(BindException.class)
    public ErrorCode bindException(BindException e) {
        log.error("BindException: {}", e.getMessage());
        return new ErrorResponse(errorCodeI18nService.buildErrorCode(ARGUMENT_VALIDATION_FAILED), e.getBindingResult());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ErrorCode duplicateKeyException(DuplicateKeyException e) {
        log.error("DuplicateKeyException: " + e.getMessage(), e);
        return errorCodeI18nService.buildErrorCode(DUPLICATE_KEY_EXCEPTION);
    }

    @ExceptionHandler(Exception.class)
    public ErrorCode exception(Exception e) {
        log.error("Unknown Exception", e);
        return errorCodeI18nService.buildErrorCode(INTERNAL_ERROR);
    }

    @ExceptionHandler(ErrorCodeException.class)
    public ErrorCode errorCodeException(ErrorCodeException e) {
        log.warn("ErrorCodeException: {}", e.getMessage());
        return errorCodeI18nService.buildErrorCode(e.getErrorCode());
    }

}
