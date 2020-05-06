package win.doyto.query.annotation;

import java.lang.annotation.Target;

/**
 * SubQuery
 *
 * @author f0rb on 2019-05-28
 */
@Target({})
public @interface NestedQuery {

    String select();

    String from();

    String extra() default "";

    String op() default "IN";

}
