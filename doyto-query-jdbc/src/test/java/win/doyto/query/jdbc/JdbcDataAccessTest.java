/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import win.doyto.query.core.PageList;
import win.doyto.query.test.perm.PermissionQuery;
import win.doyto.query.test.role.RoleEntity;
import win.doyto.query.test.role.RoleQuery;
import win.doyto.query.test.user.UserEntity;
import win.doyto.query.test.user.UserQuery;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JdbcDataAccessTest
 *
 * @author f0rb on 2021-12-04
 */
class JdbcDataAccessTest extends JdbcApplicationTest {

    private final JdbcDataAccess<RoleEntity, Integer, RoleQuery> roleDataAccess;
    private final JdbcDataAccess<UserEntity, Long, UserQuery> userDataAccess;

    public JdbcDataAccessTest(@Autowired DatabaseOperations databaseOperations) {
        this.roleDataAccess = new JdbcDataAccess<>(databaseOperations, RoleEntity.class);
        this.userDataAccess = new JdbcDataAccess<>(databaseOperations, UserEntity.class);
    }

    @Test
    void page() {
        PageList<UserEntity> pageList = userDataAccess.page(UserQuery.builder().pageSize(2).build());
        assertThat(pageList.getTotal()).isEqualTo(4);
        assertThat(pageList.getList()).hasSize(2);
    }

    @Test
    void count() {
        long validRoleCount = userDataAccess.count(UserQuery.builder().valid(true).build());
        assertThat(validRoleCount).isEqualTo(3);
    }

    @Test
    void deleteByPage() {
        roleDataAccess.delete(RoleQuery.builder().pageNumber(2).pageSize(2).build());
        List<RoleEntity> roleEntities = roleDataAccess.query(RoleQuery.builder().build());
        assertThat(roleEntities)
                .extracting("id")
                .containsExactly(1, 2, 5);
    }

    @Test
    void shouldNotDeleteWhenNothingFound() {
        int ret = roleDataAccess.delete(RoleQuery.builder().roleNameLike("noop").build());
        assertThat(ret).isZero();
    }

    @Test
    void updateByPage() {
        RoleEntity patch = new RoleEntity();
        patch.setValid(false);
        RoleQuery roleQuery = RoleQuery.builder().pageNumber(2).pageSize(2).build();
        roleDataAccess.patch(patch, roleQuery);

        List<RoleEntity> roleEntities = roleDataAccess.query(RoleQuery.builder().sort("id").build());
        assertThat(roleEntities)
                .extracting("valid")
                .containsExactly(true, true, false, false, true);
    }

    @Test
    void shouldNotUpdateWhenNothingFound() {
        RoleEntity patch = new RoleEntity();
        patch.setValid(false);

        int ret = roleDataAccess.patch(patch, RoleQuery.builder().roleNameLike("noop").build());
        assertThat(ret).isZero();
    }

    @DisplayName("An example comparing two ways of constructing query objects")
    @ParameterizedTest
    @CsvSource({
            "admin, 1 3 4",
            "vip, 1 4",
    })
    void queryUserByPermAndDifferentRoleCondition(String roleNameLike, String ids) {
        List<Long> expected = Arrays.stream(ids.split(" ")).map(Long::valueOf).toList();

        RoleQuery roleQuery = RoleQuery.builder().roleNameLike(roleNameLike).build();
        UserQuery userQuery = UserQuery
                .builder()
                .perm(PermissionQuery.builder().permNameStart("user").build())
                .role(roleQuery)
                .sort("ID")
                .build();

        List<Long> userIds = userDataAccess.queryIds(userQuery);

        PermissionQuery permQuery = PermissionQuery
                .builder().roleQuery(roleQuery).permNameStart("user").build();
        UserQuery userQuery2 = UserQuery
                .builder()
                .perm(permQuery)
                .sort("ID")
                .build();

        List<Long> userIds2 = userDataAccess.queryIds(userQuery2);

        assertThat(userIds).containsExactlyElementsOf(expected);
        assertThat(userIds2).containsExactlyElementsOf(expected);
    }
}