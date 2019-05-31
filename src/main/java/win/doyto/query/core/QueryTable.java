package win.doyto.query.core;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * QueryTable
 *
 * @author f0rb
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface QueryTable {
    String table();
}
