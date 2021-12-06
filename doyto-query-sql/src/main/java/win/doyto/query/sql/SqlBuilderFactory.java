package win.doyto.query.sql;

import lombok.experimental.UtilityClass;
import win.doyto.query.entity.Persistable;

/**
 * SqlBuilderFactory
 *
 * @author f0rb on 2021-11-21
 */
@UtilityClass
public class SqlBuilderFactory {
    public static <E extends Persistable<?>> SqlBuilder<E> create(Class<E> entityClass) {
        return new CrudBuilder<>(entityClass);
    }
}
