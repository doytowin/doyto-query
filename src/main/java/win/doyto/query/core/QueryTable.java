package win.doyto.query.core;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * QueryTable
 *
 * @author f0rb
 * @deprecated useless
 */
@Deprecated
@Target(TYPE)
@Retention(RUNTIME)
@SuppressWarnings("squid:S1133")
public @interface QueryTable {
    String table();
}
