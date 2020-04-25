package win.doyto.query.web.response;

import lombok.Getter;
import lombok.experimental.Delegate;
import org.springframework.validation.BindingResult;

import java.util.*;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Path;

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

    public ErrorResponse(ErrorCode errorCode, BindingResult bindingResult) {
        this.errorCode = errorCode;
        this.hints = buildHints(bindingResult);
    }

    public ErrorResponse(ErrorCode errorCode, List<BindingResult> bindingResults) {
        this.errorCode = errorCode;
        this.hints = bindingResults.stream().map(this::buildHints).collect(Collectors.toList());
    }

    private Map<String, String> buildHints(BindingResult bindingResult) {
        Map<String, String> error = new HashMap<>();
        bindingResult.getFieldErrors().stream()
                     .filter(fieldError -> !error.containsKey(fieldError.getField()))
                     .forEach(fieldError -> error.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return error;
    }

    public ErrorResponse(ErrorCode errorCode, Set<ConstraintViolation<?>> constraintViolations) {
        this.errorCode = errorCode;
        HashMap<String, String> error = new HashMap<>();

        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            Iterator<Path.Node> iterator = constraintViolation.getPropertyPath().iterator();
            while (iterator.hasNext()) {
                Path.Node last = iterator.next();
                if (!iterator.hasNext()) {
                    error.put(last.getName(), constraintViolation.getMessage());
                    break;
                }
            }
        }
        this.hints = error;
    }
}
