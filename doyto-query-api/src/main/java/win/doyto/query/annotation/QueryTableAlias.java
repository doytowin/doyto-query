package win.doyto.query.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * QueryTableAlias
 *
 * @author f0rb on 2019-06-11
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface QueryTableAlias {
    String value();
}
