package win.doyto.query.web.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 返回给移动客户端的JSON对象的结构
 *
 * @author Yuanzhen on 2015-09-07.
 */
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class JsonResponse<T> implements ErrorCode {

    private Integer code = 0;
    private String message = "ok";
    private T data;
    private Map<String, List<String>> errors;

    public JsonResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public void addError(String fieldName, String message) {
        final Map<String, List<String>> messages = internalGetErrors();
        List<String> fieldMessages = messages.computeIfAbsent(fieldName, k -> new ArrayList<>());
        fieldMessages.add(message);
    }

    private synchronized Map<String, List<String>> internalGetErrors() {
        if (errors == null) {
            errors = new ConcurrentHashMap<>();
        }
        return errors;
    }

}
