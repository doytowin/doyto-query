package win.doyto.query.web.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * JsonResponse
 *
 * @author f0rb on 2021-10-30
 */
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class JsonResponse<T> implements ErrorCode {

    private Integer code = 0;
    private String message = "OK";
    private T data;

}
