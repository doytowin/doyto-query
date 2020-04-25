package win.doyto.query.web.response;

import lombok.Getter;
import lombok.experimental.Delegate;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;

/**
 * 返回给移动客户端的JSON对象的结构
 *
 * @author Yuanzhen on 2015-09-07.
 */
public class ErrorResponse implements ErrorCode {

    @Delegate
    private ErrorCode errorCode;
    @Getter
    private Object hints;

    public ErrorResponse(ErrorCode errorCode, List<FieldError> fieldErrors) {
        this.errorCode = errorCode;
        HashMap<String, String> error = new HashMap<>();
        fieldErrors.stream()
                   .filter(fieldError -> !error.containsKey(fieldError.getField()))
                   .forEach(fieldError -> error.put(fieldError.getField(), fieldError.getDefaultMessage()));
        this.hints = error;
    }

}
