package win.doyto.query.web.response;

import java.lang.annotation.*;

/**
 * ErrorCode
 *
 * @author f0rb on 2017-03-05.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonBody {
}
