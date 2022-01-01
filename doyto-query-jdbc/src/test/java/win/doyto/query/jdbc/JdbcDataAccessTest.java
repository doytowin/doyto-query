/*
 * Copyright © 2019-2022 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.jdbc;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import win.doyto.query.sql.SqlAndArgs;
import win.doyto.query.test.role.RoleEntity;
import win.doyto.query.test.role.RoleQuery;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * JdbcDataAccessTest
 *
 * @author f0rb on 2021-12-04
 */
class JdbcDataAccessTest extends JdbcApplicationTest {

    private JdbcDataAccess<RoleEntity, Integer, RoleQuery> jdbcDataAccess;
    private DatabaseTemplate databaseOperations;

    public JdbcDataAccessTest(@Autowired JdbcOperations jdbcOperations) {
        this.databaseOperations = spy(new DatabaseTemplate(jdbcOperations));
        this.jdbcDataAccess = new JdbcDataAccess<>(databaseOperations, RoleEntity.class, new BeanPropertyRowMapper<>(RoleEntity.class));
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
                .containsExactly(1, 2, 5);
    }

    @Test
    void shouldNotDeleteWhenNothingFound() {
        jdbcDataAccess.delete(RoleQuery.builder().roleNameLike("noop").build());
        verify(databaseOperations, times(0)).update(any(SqlAndArgs.class));
    }

    @Test
    void updateByPage() {
        RoleEntity patch = new RoleEntity();
        patch.setValid(false);

        jdbcDataAccess.patch(patch, RoleQuery.builder().pageNumber(1).pageSize(2).build());
        List<RoleEntity> query = jdbcDataAccess.query(RoleQuery.builder().sort("id").build());
        assertThat(query)
                .extracting("valid")
                .containsExactly(true, true, false, false, true);
    }

    @Test
    void shouldNotUpdateWhenNothingFound() {
        RoleEntity patch = new RoleEntity();
        patch.setValid(false);

        jdbcDataAccess.patch(patch, RoleQuery.builder().roleNameLike("noop").build());

        verify(databaseOperations, times(0)).update(any(SqlAndArgs.class));
    }
}