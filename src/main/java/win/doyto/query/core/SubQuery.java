package win.doyto.query.core;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * SubQuery
 *
 * @author f0rb on 2019-05-28
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface SubQuery {

    String column() default "id";
    String op() default "IN";
    String left();
    String table();
    String right() default "";
    String extra() default "";
}
