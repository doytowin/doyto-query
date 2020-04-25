package win.doyto.query.web.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 返回给移动客户端的JSON对象的结构
 *
 * @author Yuanzhen on 2015-09-07.
 */
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class JsonResponse<T> implements ErrorCode {

    private Integer code = 0;
    private String message = "ok";
    private T data;

}
