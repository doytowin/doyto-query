package win.doyto.query.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Joins
 *
 * @author f0rb on 2019-06-09
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Joins {
    Join[] value();

    @Target({})
    @interface Join {
        String value();
    }
}
