package win.doyto.query.web.response;

import java.lang.annotation.*;

/**
 * JsonBody
 *
 * @author f0rb on 2021-10-30
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonBody {
}
