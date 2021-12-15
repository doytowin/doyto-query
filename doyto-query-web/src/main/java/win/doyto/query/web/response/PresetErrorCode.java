package win.doyto.query.web.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**
 * PresetErrorCode
 *
 * @author f0rb
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PresetErrorCode implements ErrorCode {
    SUCCESS,

    INTERNAL_ERROR,
    REQUEST_BODY_ERROR,
    ARGUMENT_TYPE_MISMATCH,
    ARGUMENT_FORMAT_ERROR,
    ARGUMENT_VALIDATION_FAILED,
    HTTP_METHOD_NOT_SUPPORTED,
    DUPLICATE_KEY_EXCEPTION,
    FILE_UPLOAD_OVER_MAX_SIZE,
    ENTITY_NOT_FOUND,

    ;

    private final Integer code;

    PresetErrorCode() {
        this.code = Index.count++;
    }

    public String getMessage() {
        return super.name();
    }

    private static class Index {
        private static int count = 0;
    }
}
