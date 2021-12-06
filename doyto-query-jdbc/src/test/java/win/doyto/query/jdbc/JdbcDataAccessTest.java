package win.doyto.query.jdbc;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcOperations;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JdbcDataAccessTest
 *
 * @author f0rb on 2021-12-04
 */
class JdbcDataAccessTest {
    @Test
    void constructorForJdbcOperationsAndEntityClass() {
        Constructor<JdbcDataAccess> constructor =
                ConstructorUtils.getAccessibleConstructor(JdbcDataAccess.class, JdbcOperations.class, Class.class);
        assertNotNull(constructor);
    }
}