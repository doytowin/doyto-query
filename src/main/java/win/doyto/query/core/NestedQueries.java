package win.doyto.query.core;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * NestedQueries
 *
 * @author f0rb on 2019-05-28
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface NestedQueries {

    String column() default "id";

    String op() default Constant.IN;

    NestedQuery[] value();

}
