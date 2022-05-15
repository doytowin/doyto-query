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
import org.springframework.jdbc.core.JdbcOperations;
import win.doyto.query.test.role.RoleEntity;
import win.doyto.query.test.role.RoleQuery;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JdbcDataAccessTest
 *
 * @author f0rb on 2021-12-04
 */
class JdbcDataAccessTest extends JdbcApplicationTest {

    private JdbcDataAccess<RoleEntity, Integer, RoleQuery> jdbcDataAccess;

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
        jdbcDataAccess.delete(RoleQuery.builder().pageNumber(2).pageSize(2).build());
        List<RoleEntity> roleEntities = jdbcDataAccess.query(RoleQuery.builder().build());
        assertThat(roleEntities)
                .extracting("id")
                .containsExactly(1, 2, 5);
    }

    @Test
    void shouldNotDeleteWhenNothingFound() {
        int ret = jdbcDataAccess.delete(RoleQuery.builder().roleNameLike("noop").build());
        assertThat(ret).isZero();
    }

    @Test
    void updateByPage() {
        RoleEntity patch = new RoleEntity();
        patch.setValid(false);
        RoleQuery roleQuery = RoleQuery.builder().pageNumber(2).pageSize(2).build();
        jdbcDataAccess.patch(patch, roleQuery);

        List<RoleEntity> roleEntities = jdbcDataAccess.query(RoleQuery.builder().sort("id").build());
        assertThat(roleEntities)
                .extracting("valid")
                .containsExactly(true, true, false, false, true);
    }

    @Test
    void shouldNotUpdateWhenNothingFound() {
        RoleEntity patch = new RoleEntity();
        patch.setValid(false);

        int ret = jdbcDataAccess.patch(patch, RoleQuery.builder().roleNameLike("noop").build());
        assertThat(ret).isZero();
    }
}