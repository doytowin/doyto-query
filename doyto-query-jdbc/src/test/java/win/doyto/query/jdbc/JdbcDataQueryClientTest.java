/*
 * Copyright © 2019-2024 Forb Yuan
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

import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.PageList;
import win.doyto.query.test.menu.MenuEntity;
import win.doyto.query.test.menu.MenuQuery;
import win.doyto.query.test.perm.PermissionQuery;
import win.doyto.query.test.role.RoleEntity;
import win.doyto.query.test.role.RoleQuery;
import win.doyto.query.test.user.UserEntity;
import win.doyto.query.test.user.UserQuery;

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

    private DataAccess<RoleEntity, Integer, RoleQuery> roleDataAccess;

    public JdbcDataQueryClientTest(@Autowired DatabaseOperations databaseOperations) {
        this.roleDataAccess = new JdbcDataAccess<>(databaseOperations, RoleEntity.class);
    }

    @Test
    void queryForJoin() {
        UserQuery usersQuery = UserQuery.builder().build();
        RoleQuery roleQuery = RoleQuery.builder().user(usersQuery).withUsers(usersQuery).sort("id,asc").build();
        List<RoleEntity> roleEntities = roleDataAccess.query(roleQuery);
        assertThat(roleEntities)
                .extracting(roleEntity -> roleEntity.getUsers().size())
                .containsExactly(3, 2);
    }

    @Test
    void countForGroupBy() {
        RoleQuery roleQuery = RoleQuery.builder().user(new UserQuery()).build();
        long count = roleDataAccess.count(roleQuery);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void pageForJoin() {
        RoleQuery roleQuery = RoleQuery.builder().roleName("vip").build();
        RoleQuery rolesQuery = RoleQuery.builder().roleNameLike("vip").build();
        UserQuery userQuery = UserQuery.builder().role(roleQuery).withRoles(rolesQuery).build();
        PageList<UserEntity> page = jdbcDataQueryClient.page(userQuery, UserEntity.class);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getList()).extracting(UserEntity::getUsername).containsExactly("f0rb", "user4");
        assertThat(page.getList()).extracting(it -> it.getRoles().size()).containsExactly(1, 1);
    }

    @Test
    void pageForJoinWithSize10() {
        PageList<MenuEntity> page = jdbcDataQueryClient.page(MenuQuery.builder().build(), MenuEntity.class);
        assertThat(page.getTotal()).isEqualTo(12);
        assertThat(page.getList()).hasSize(10);
    }

    @Test
    void queryUserWithRoles() {
        UserQuery userQuery = UserQuery
                .builder().withRoles(new RoleQuery()).withPerms(new PermissionQuery()).build();

        List<UserEntity> users = jdbcDataQueryClient.query(userQuery, UserEntity.class);

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
        List<UserEntity> users = jdbcDataQueryClient.query(UserQuery.builder().build(), UserEntity.class);
        assertThat(users).hasSize(4);
        assertThat(users).extracting("roles").containsOnlyNulls();
        assertThat(users).extracting("perms").containsOnlyNulls();
    }

    @Test
    void queryRoleWithUsersAndPerms() {
        RoleQuery roleQuery = RoleQuery.builder()
                                       .withUsers(new UserQuery())
                                       .withPerms(new PermissionQuery())
                                       .build();

        List<RoleEntity> roles = roleDataAccess.query(roleQuery);

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
        RoleQuery roleQuery = RoleQuery.builder().withCreateUser(new UserQuery()).build();

        List<RoleEntity> roles = roleDataAccess.query(roleQuery);

        assertThat(roles).map(RoleEntity::getCreateUser)
                         .extracting(userView -> userView == null ? null : userView.getId())
                         .containsExactly(1L, 2L, 2L, null, null);
    }

    /**
     * A full testcase for subdomains query
     * <p>
     * {@link UserEntity#getMenus()} for <b>many-to-many</b><br>
     * {@link UserEntity#getCreateUser()} for <b>many-to-one</b><br>
     * {@link UserEntity#getCreateRoles()} ()} for <b>one-to-many</b>
     */
    @Test
    void queryUserWithGrantedMenusAndCreatedRolesAndCreateUser() {
        UserQuery userQuery = UserQuery
                .builder()
                .withMenus(new MenuQuery())
                .withCreateUser(new UserQuery())
                .withCreateRoles(new RoleQuery())
                .build();

        List<UserEntity> users = jdbcDataQueryClient.query(userQuery, UserEntity.class);

        assertThat(users).extracting("menus")
                         .extractingResultOf("size", Integer.class)
                         .containsExactly(7, 0, 3, 7);
        assertThat(users).map(UserEntity::getCreateUser)
                         .extracting(userView -> userView == null ? null : userView.getId())
                         .containsExactly(1L, 1L, 2L, 2L);
        assertThat(users).extracting("createRoles")
                         .extractingResultOf("size", Integer.class)
                         .containsExactly(1, 2, 0, 0);
    }

    @DisplayName("An example for the combination of nested query and related query")
    @Test
    void queryParentMenuForMenu10WithParentAndValidChildrenMenu() {
        MenuQuery parentForMenu10 = MenuQuery.builder().id(10L).build();
        MenuQuery menuQuery = MenuQuery.builder()
                                       .children(parentForMenu10)
                                       .withParent(new MenuQuery())
                                       .withChildren(MenuQuery.builder().valid(true).build())
                                       .build();
        List<MenuEntity> menus = jdbcDataQueryClient.query(menuQuery, MenuEntity.class);
        assertThat(menus).hasSize(1);
        MenuEntity menu = menus.get(0);
        assertThat(menu.getId()).isEqualTo(4);
        assertThat(menu.getParent().getId()).isEqualTo(1);
        assertThat(menu.getChildren()).hasSize(3);
    }
}
