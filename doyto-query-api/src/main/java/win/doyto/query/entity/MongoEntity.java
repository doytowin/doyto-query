package win.doyto.query.entity;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * MongoEntity
 *
 * @author f0rb on 2021-12-04
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface MongoEntity {

    String database();

    String collection();

}
