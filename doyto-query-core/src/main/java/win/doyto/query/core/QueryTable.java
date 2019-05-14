package win.doyto.query.core;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * QueryTable
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface QueryTable {
    String table();

    Class<?> entityClass();
}
