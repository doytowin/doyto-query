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

package win.doyto.query.sql;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import win.doyto.query.test.TestQuery;
import win.doyto.query.test.menu.MenuQuery;
import win.doyto.query.test.perm.PermissionQuery;
import win.doyto.query.test.role.RoleQuery;
import win.doyto.query.test.user.UserLevel;
import win.doyto.query.test.user.UserQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DomainPathProcessorTest
 *
 * @author f0rb on 2022-04-23
 */
@ResourceLock(value = "mapCamelCaseToUnderscore")
class DomainPathProcessorTest {

    List<Object> argList = new ArrayList<>();

    @SneakyThrows
    private static DomainPathProcessor buildProcessor(Class<?> clazz, String fieldName) {
        return new DomainPathProcessor(clazz.getDeclaredField(fieldName));
    }

    @Test
    void supportNestedQueryWithTwoDomains() {
        DomainPathProcessor domainPathProcessor = buildProcessor(UserQuery.class, "role");
        RoleQuery roleQuery = RoleQuery.builder().id(1).valid(true).build();

        String sql = domainPathProcessor.process(argList, roleQuery);

        String expected = "id IN (" +
                "SELECT user_id FROM a_user_and_role WHERE role_id IN (" +
                "SELECT id FROM t_role WHERE id = ? AND valid = ?" +
                "))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).containsExactly(1, true);
    }

    @Test
    void supportReverseNestedQueryWithTwoDomains() {
        DomainPathProcessor domainPathProcessor = buildProcessor(PermissionQuery.class, "role");

        String sql = domainPathProcessor.process(argList, new RoleQuery());

        String expected = "id IN (" +
                "SELECT perm_id FROM a_role_and_perm WHERE role_id IN (" +
                "SELECT id FROM t_role" +
                "))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).isEmpty();
    }

    @Test
    void supportReverseNestedQueryWithTwoDomainsAndConditions() {
        DomainPathProcessor domainPathProcessor = buildProcessor(PermissionQuery.class, "role");
        RoleQuery roleQuery = RoleQuery.builder().id(1).valid(true).build();

        String sql = domainPathProcessor.process(argList, roleQuery);

        String expected = "id IN (" +
                "SELECT perm_id FROM a_role_and_perm WHERE role_id IN (" +
                "SELECT id FROM t_role WHERE id = ? AND valid = ?" +
                "))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).containsExactly(1, true);
    }

    @Test
    void buildSubQueryWithCollection() {
        DomainPathProcessor domainPathProcessor = buildProcessor(PermissionQuery.class, "role");
        RoleQuery roleQuery = RoleQuery.builder().idIn(Arrays.asList(1, 2, 3)).build();

        String sql = domainPathProcessor.process(argList, roleQuery);

        String expected = "id IN (" +
                "SELECT perm_id FROM a_role_and_perm WHERE role_id IN (" +
                "SELECT id FROM t_role WHERE id IN (?, ?, ?)" +
                "))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).containsExactly(1, 2, 3);
    }

    @Test
    void buildSubQueryWithNullCollection() {
        DomainPathProcessor domainPathProcessor = buildProcessor(PermissionQuery.class, "role");
        RoleQuery roleQuery = RoleQuery.builder().idIn(Collections.emptyList()).build();

        String sql = domainPathProcessor.process(argList, roleQuery);

        String expected = "id IN (" +
                "SELECT perm_id FROM a_role_and_perm WHERE role_id IN (" +
                "SELECT id FROM t_role WHERE id IN (null)" +
                "))";

        assertThat(sql).isEqualTo(expected);
        assertThat(argList).isEmpty();
    }

    @Test
    void supportNestedQueryWithThreeDomains() {
        DomainPathProcessor domainPathProcessor = buildProcessor(UserQuery.class, "perm");

        String sql = domainPathProcessor.process(argList, new PermissionQuery());

        String expected = "id IN (" +
                "SELECT user_id FROM a_user_and_role WHERE role_id IN (" +
                "SELECT role_id FROM a_role_and_perm WHERE perm_id IN (" +
                "SELECT id FROM t_perm" +
                ")))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).isEmpty();
    }

    @Test
    void supportReverseNestedQueryWithThreeDomains() {
        DomainPathProcessor domainPathProcessor = buildProcessor(PermissionQuery.class, "user");

        String sql = domainPathProcessor.process(argList, new UserQuery());

        String expected = "id IN (" +
                "SELECT perm_id FROM a_role_and_perm WHERE role_id IN (" +
                "SELECT role_id FROM a_user_and_role WHERE user_id IN (" +
                "SELECT id FROM t_user" +
                ")))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).isEmpty();
    }

    @Test
    void supportReverseNestedQueryWithThreeDomainsAndSimpleConditionForLastDomain() {
        DomainPathProcessor domainPathProcessor = buildProcessor(PermissionQuery.class, "user");
        UserQuery userQuery = UserQuery.builder().id(2).build();

        String sql = domainPathProcessor.process(argList, userQuery);

        String expected = "id IN (SELECT perm_id FROM a_role_and_perm WHERE role_id IN " +
                "(SELECT role_id FROM a_user_and_role WHERE user_id IN (" +
                "SELECT id FROM t_user WHERE id = ?" +
                ")))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).containsExactly(2);
    }

    @Test
    void supportReverseNestedQueryWithThreeDomainsAndQueryForLastDomain() {
        DomainPathProcessor domainPathProcessor = buildProcessor(PermissionQuery.class, "user");
        UserQuery userQuery = UserQuery.builder().usernameLike("test").userLevel(UserLevel.普通).build();

        String sql = domainPathProcessor.process(argList, userQuery);

        String expected = "id IN (SELECT perm_id FROM a_role_and_perm WHERE role_id IN " +
                "(SELECT role_id FROM a_user_and_role WHERE user_id IN " +
                "(SELECT id FROM t_user WHERE username LIKE ? AND user_level = ?)))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).containsExactly("%test%", UserLevel.普通.ordinal());
    }

    @Test
    void supportReverseNestedQueryWithFourDomainsAndQueryForEachDomain() {
        DomainPathProcessor domainPathProcessor = buildProcessor(MenuQuery.class, "user");
        UserQuery userQuery = UserQuery
                .builder()
                .permQuery(PermissionQuery.builder().valid(true).build())
                .roleQuery(RoleQuery.builder().roleNameLike("vip").valid(true).build())
                .id(1).build();

        String sql = domainPathProcessor.process(argList, userQuery);

        String expected = "id IN (" +
                "SELECT menu_id FROM a_perm_and_menu WHERE perm_id IN (" +
                "SELECT id FROM t_perm WHERE valid = ?" +
                "\nINTERSECT\n" +
                "SELECT perm_id FROM a_role_and_perm WHERE role_id IN (" +
                "SELECT id FROM t_role WHERE role_name LIKE ? AND valid = ?" +
                "\nINTERSECT\n" +
                "SELECT role_id FROM a_user_and_role WHERE user_id IN (" +
                "SELECT id FROM t_user WHERE id = ?))))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).containsExactly(true, "%vip%", true, 1);
    }

    @Test
    void supportSelfOneToManyQuery() {
        DomainPathProcessor domainPathProcessor = buildProcessor(MenuQuery.class, "parentQuery");
        MenuQuery parentQuery = MenuQuery.builder().nameLike("test").valid(true).build();

        String sql = domainPathProcessor.process(argList, parentQuery);

        String expected = "id IN (SELECT parent_id FROM t_menu WHERE name LIKE ? AND valid = ?)";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).containsExactly("%test%", true);
    }

    @Test
    void supportSelfManyToOneQuery() {
        DomainPathProcessor domainPathProcessor = buildProcessor(MenuQuery.class, "childrenQuery");
        MenuQuery parentQuery = MenuQuery.builder().nameLike("test").valid(true).build();

        String sql = domainPathProcessor.process(argList, parentQuery);

        String expected = "parent_id IN (SELECT id FROM t_menu WHERE name LIKE ? AND valid = ?)";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).containsExactly("%test%", true);
    }

    @Test
    void supportForeignOneToManyQuery() {
        DomainPathProcessor domainPathProcessor = buildProcessor(TestQuery.class, "createUser");
        UserQuery createUserQuery = UserQuery.builder().id(1).build();

        String sql = domainPathProcessor.process(argList, createUserQuery);

        assertThat(sql).isEqualTo("create_user_id IN (SELECT id FROM t_user WHERE id = ?)");
        assertThat(argList).containsExactly(1);
    }
}