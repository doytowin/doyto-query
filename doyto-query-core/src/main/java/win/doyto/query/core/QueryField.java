package win.doyto.query.core;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * QueryField
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface QueryField {
    String and();
}
