package win.doyto.query.annotation;

/**
 * SubQuery
 *
 * @author f0rb on 2019-05-28
 */
public @interface NestedQuery {

    String left();

    String table();

    String extra() default "";

    String op() default "IN";

}
