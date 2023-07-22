/*
 * Copyright © 2019-2023 Forb Yuan
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

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.PageList;
import win.doyto.query.test.menu.MenuQuery;
import win.doyto.query.test.menu.MenuView;
import win.doyto.query.test.menu.MenuViewQuery;
import win.doyto.query.test.perm.PermissionQuery;
import win.doyto.query.test.role.RoleQuery;
import win.doyto.query.test.role.RoleView;
import win.doyto.query.test.role.RoleViewQuery;
import win.doyto.query.test.user.*;

import javax.annotation.Resource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JdbcDataQueryTest
 *
 * @author f0rb on 2020-04-11
 */
class JdbcDataQueryClientTest extends JdbcApplicationTest {
    @Resource
    private JdbcDataQueryClient jdbcDataQueryClient;

    @Test
    void queryForJoin() {
        UserQuery usersQuery = UserQuery.builder().build();
        RoleViewQuery roleQuery = RoleViewQuery.builder().user(usersQuery).withUsers(usersQuery).sort("id,asc").build();
        List<RoleView> roleViews = jdbcDataQueryClient.query(roleQuery);
        assertThat(roleViews)
                .extracting(roleView -> roleView.getUsers().size())
                .containsExactly(3, 2);
    }

    @Test
    void countForGroupBy() {
        RoleViewQuery roleQuery = RoleViewQuery.builder().user(new UserQuery()).build();
        long count = jdbcDataQueryClient.count(roleQuery);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void pageForJoin() {
        RoleQuery roleQuery = RoleQuery.builder().roleName("vip").build();
        RoleQuery rolesQuery = RoleQuery.builder().roleNameLike("vip").build();
        UserViewQuery userViewQuery = UserViewQuery.builder().role(roleQuery).withRoles(rolesQuery).build();
        PageList<UserView> page = jdbcDataQueryClient.page(userViewQuery);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getList()).extracting(UserView::getUsername).containsExactly("f0rb", "user4");
        assertThat(page.getList()).extracting(it -> it.getRoles().size()).containsExactly(1, 1);
    }

    @Test
    void pageForJoinWithSize10() {
        PageList<MenuView> page = jdbcDataQueryClient.page(MenuQuery.builder().build(), MenuView.class);
        assertThat(page.getTotal()).isEqualTo(12);
        assertThat(page.getList()).hasSize(10);
    }

    @Test
    void queryUserWithRoles() {
        UserViewQuery userViewQuery = UserViewQuery
                .builder().withRoles(new RoleQuery()).withPerms(new PermissionQuery()).build();

        List<UserView> users = jdbcDataQueryClient.query(userViewQuery);

        assertThat(users).extracting("roles")
                         .extractingResultOf("size", Integer.class)
                         .containsExactly(2, 0, 1, 2);
        assertThat(users.get(0).getRoles())
                .hasSize(2)
                .flatExtracting("id", "roleName", "roleCode")
                .containsExactly(1, "admin", "ADMIN", 2, "vip", "VIP");
        assertThat(users).extracting("perms")
                         .extractingResultOf("size", Integer.class)
                         .containsExactly(4, 0, 2, 4);
    }

    @Test
    void shouldNotQuerySubDomainWhenItsQueryFieldIsNull() {
        List<UserView> users = jdbcDataQueryClient.query(UserViewQuery.builder().build());
        assertThat(users).hasSize(4);
        assertThat(users).extracting("roles").containsOnlyNulls();
        assertThat(users).extracting("perms").containsOnlyNulls();
    }

    @Test
    void queryRoleWithUsersAndPerms() {
        RoleViewQuery roleQuery = RoleViewQuery.builder()
                                               .withUsers(new UserQuery())
                                               .withPerms(new PermissionQuery())
                                               .build();

        List<RoleView> roles = jdbcDataQueryClient.query(roleQuery);

        assertThat(roles)
                .extracting("perms")
                .extractingResultOf("size", Integer.class)
                .containsExactly(2, 3, 0, 0, 2);
        assertThat(roles)
                .extracting("users")
                .extractingResultOf("size", Integer.class)
                .containsExactly(3, 2, 0, 0, 0);
    }

    @Test
    void queryRoleWithCreateUser() {
        RoleViewQuery roleQuery = RoleViewQuery.builder().withCreateUser(new UserQuery()).build();

        List<RoleView> roles = jdbcDataQueryClient.query(roleQuery);

        assertThat(roles).map(RoleView::getCreateUser)
                         .extracting(userView -> userView == null ? null : userView.getId())
                         .containsExactly(1L, 2L, 2L, null, null);
    }

    /**
     * A full testcase for subdomains query
     * <p>
     * {@link UserView#getMenus()} for <b>many-to-many</b><br>
     * {@link UserView#getCreateUser()} for <b>many-to-one</b><br>
     * {@link UserView#getCreateRoles()} ()} for <b>one-to-many</b>
     */
    @Test
    void queryUserWithGrantedMenusAndCreatedRolesAndCreateUser() {
        UserViewQuery userViewQuery = UserViewQuery
                .builder()
                .withMenus(new MenuQuery())
                .withCreateUser(new UserQuery())
                .withCreateRoles(new RoleQuery())
                .build();

        List<UserView> users = jdbcDataQueryClient.query(userViewQuery);

        assertThat(users).extracting("menus")
                         .extractingResultOf("size", Integer.class)
                         .containsExactly(7, 0, 3, 7);
        assertThat(users).map(UserView::getCreateUser)
                         .extracting(userView -> userView == null ? null : userView.getId())
                         .containsExactly(1L, 1L, 2L, 2L);
        assertThat(users).extracting("createRoles")
                         .extractingResultOf("size", Integer.class)
                         .containsExactly(1, 2, 0, 0);
    }

    @Test
    void supportAggregateQuery() {
        UserLevelQuery query = new UserLevelQuery();
        List<UserLevelCountView> userLevelCountViews = jdbcDataQueryClient.aggregate(query, UserLevelCountView.class);
        assertThat(userLevelCountViews)
                .hasSize(3)
                .extracting("userLevel", "valid", "count")
                .containsExactlyInAnyOrder(
                        new Tuple(UserLevel.高级, true, 1L),
                        new Tuple(UserLevel.普通, false, 1L),
                        new Tuple(UserLevel.普通, true, 2L)
                );
    }

    @DisplayName("An example for the combination of nested query and related query")
    @Test
    void queryParentMenuForMenu10WithParentAndValidChildrenMenu() {
        MenuQuery parentForMenu10 = MenuQuery.builder().id(10L).build();
        MenuViewQuery menuQuery = MenuViewQuery.builder()
                                               .children(parentForMenu10)
                                               .withParent(new MenuQuery())
                                               .withChildren(MenuQuery.builder().valid(true).build())
                                               .build();
        List<MenuView> menus = jdbcDataQueryClient.query(menuQuery);
        assertThat(menus).hasSize(1);
        MenuView menu = menus.get(0);
        assertThat(menu.getId()).isEqualTo(4);
        assertThat(menu.getParent().getId()).isEqualTo(1L);
        assertThat(menu.getChildren()).hasSize(3);
    }
}
