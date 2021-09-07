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

    /**
     * @return some join clause.
     */
    String extra() default "";

    /**
     * Will use next @NestedQuery.select() as column if empty.
     *
     * @return custom column for next nested query.
     */
    String where() default "";

    String op() default "IN";

}
