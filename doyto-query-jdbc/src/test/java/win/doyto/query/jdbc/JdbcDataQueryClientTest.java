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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import win.doyto.query.service.PageList;
import win.doyto.query.test.PermissionQuery;
import win.doyto.query.test.UserQuery;
import win.doyto.query.test.join.RoleView;
import win.doyto.query.test.join.UserJoinQuery;
import win.doyto.query.test.join.UserView;
import win.doyto.query.test.role.RoleQuery;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JdbcDataQueryTest
 *
 * @author f0rb on 2020-04-11
 */
class JdbcDataQueryClientTest extends JdbcApplicationTest {
    private JdbcDataQueryClient jdbcDataQueryClient;

    @BeforeEach
    void setUp(@Autowired JdbcOperations jdbcOperations) {
        jdbcDataQueryClient = new JdbcDataQueryClient(jdbcOperations);
    }

    @Test
    void queryForJoin() {
        UserQuery usersQuery = UserQuery.builder().build();
        RoleQuery roleQuery = RoleQuery.builder().user(usersQuery).usersQuery(usersQuery).build();
        List<RoleView> roleViews = jdbcDataQueryClient.query(roleQuery);
        assertThat(roleViews)
                .extracting(roleView -> roleView.getUsers().size())
                .containsExactly(3, 2);
    }

    @Test
    void countForGroupBy() {
        RoleQuery roleQuery = RoleQuery.builder().user(new UserQuery()).build();
        long count = jdbcDataQueryClient.count(roleQuery);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void pageForJoin() {
        RoleQuery roleQuery = RoleQuery.builder().roleName("vip").build();
        RoleQuery rolesQuery = RoleQuery.builder().roleNameLike("vip").build();
        UserJoinQuery userJoinQuery = UserJoinQuery.builder().role(roleQuery).rolesQuery(rolesQuery).build();
        PageList<UserView> page = jdbcDataQueryClient.page(userJoinQuery);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getList()).extracting(UserView::getUsername).containsExactly("f0rb", "user4");
        assertThat(page.getList()).extracting(it -> it.getRoles().size()).containsExactly(1, 1);
    }

    @Test
    void queryUserWithRoles() {
        UserJoinQuery userJoinQuery = UserJoinQuery.builder().rolesQuery(new RoleQuery()).permsQuery(new PermissionQuery()).build();

        List<UserView> users = jdbcDataQueryClient.query(userJoinQuery);

        assertThat(users).extracting("roles")
                         .extractingResultOf("size", Integer.class)
                         .containsExactly(2, 0, 1, 2);
        assertThat(users.get(0).getRoles())
                .hasSize(2)
                .flatExtracting("id", "roleName", "roleCode")
                .containsExactly(1, "admin", "ADMIN", 2, "vip", "VIP");
        assertThat(users).extracting("perms")
                         .extractingResultOf("size", Integer.class)
                         .containsExactly(2, 0, 2, 2);
    }

    @Test
    void shouldNotQuerySubDomainWhenItsQueryFieldIsNull() {
        List<UserView> users = jdbcDataQueryClient.query(UserJoinQuery.builder().build());
        assertThat(users).hasSize(4);
        assertThat(users).extracting("roles").containsOnlyNulls();
        assertThat(users).extracting("perms").containsOnlyNulls();
    }

    @Test
    void queryRoleWithUsersAndPerms() {
        RoleQuery roleQuery = RoleQuery.builder().usersQuery(new UserQuery())
                                       .permsQuery(new PermissionQuery()).build();

        List<RoleView> roles = jdbcDataQueryClient.query(roleQuery);

        assertThat(roles)
                .extracting("perms")
                .extractingResultOf("size", Integer.class)
                .containsExactly(2, 1, 0, 0, 2);
        assertThat(roles)
                .extracting("users")
                .extractingResultOf("size", Integer.class)
                .containsExactly(3, 2, 0, 0, 0);
    }
}
