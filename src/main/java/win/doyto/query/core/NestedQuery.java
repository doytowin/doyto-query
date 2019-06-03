package win.doyto.query.core;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * SubQuery
 *
 * @author f0rb on 2019-05-28
 */
@Retention(RUNTIME)
public @interface NestedQuery {

    String left();

    String table();

    String extra() default "";

    String op() default "IN";

}
