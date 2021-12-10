package win.doyto.query.jdbc;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import win.doyto.query.test.role.RoleEntity;
import win.doyto.query.test.role.RoleQuery;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JdbcDataAccessTest
 *
 * @author f0rb on 2021-12-04
 */
class JdbcDataAccessTest extends JdbcApplicationTest {

    private JdbcDataAccess<RoleEntity, Long, RoleQuery> jdbcDataAccess;

    public JdbcDataAccessTest(@Autowired JdbcOperations jdbcOperations) {
        this.jdbcDataAccess = new JdbcDataAccess<>(jdbcOperations, RoleEntity.class);
    }

    @Test
    void constructorForJdbcOperationsAndEntityClass() {
        Constructor<JdbcDataAccess> constructor =
                ConstructorUtils.getAccessibleConstructor(JdbcDataAccess.class, JdbcOperations.class, Class.class);
        assertNotNull(constructor);
    }

    @Test
    void deleteByPage() {
        jdbcDataAccess.delete(RoleQuery.builder().pageNumber(1).pageSize(2).build());
        assertThat(jdbcDataAccess.query(RoleQuery.builder().build()))
                .extracting("id")
                .containsExactly(1L, 2L, 5L);
    }
}