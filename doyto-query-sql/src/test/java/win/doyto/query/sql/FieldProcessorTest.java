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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import win.doyto.query.test.*;
import win.doyto.query.test.role.RoleQuery;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ;

/**
 * FieldProcessorTest
 *
 * @author f0rb on 2019-06-04
 */
@ResourceLock(value = "mapCamelCaseToUnderscore", mode = READ)
class FieldProcessorTest {

    private static Field field;
    private List<Object> argList = new ArrayList<>();

    @BeforeAll
    static void beforeAll() throws NoSuchFieldException {
        field = DoytoDomainQuery.class.getDeclaredField("domainRoute");
        FieldProcessor.init(field);
    }

    @Test
    void supportNestedQueryWithTwoDomains() {
        DoytoDomainRoute domainRoute = DoytoDomainRoute.builder().path(Arrays.asList("user", "role")).roleId(1).build();

        String sql = FieldProcessor.execute(field, argList, domainRoute);

        assertThat(sql).isEqualTo("id IN (SELECT userId FROM t_user_and_role WHERE roleId = ?)");
        assertThat(argList).containsExactly(1);
    }

    @Test
    void supportReverseNestedQueryWithTwoDomains() {
        DoytoDomainRoute domainRoute = DoytoDomainRoute
                .builder().path(Arrays.asList("role", "perm")).reverse(true).build();

        String sql = FieldProcessor.execute(field, argList, domainRoute);

        String expected = "id IN (SELECT permId FROM t_role_and_perm)";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).isEmpty();
    }

    @Test
    void buildSubQueryWithCollection() {
        DoytoDomainRoute domainRoute = DoytoDomainRoute
                .builder().path(Arrays.asList("role", "perm")).reverse(true)
                .roleIdIn(Arrays.asList(1, 2, 3)).build();

        String sql = FieldProcessor.execute(field, argList, domainRoute);

        String expected = "id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN (?, ?, ?))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).containsExactly(1, 2, 3);
    }

    @Test
    void buildSubQueryWithNullCollection() {
        DoytoDomainRoute domainRoute = DoytoDomainRoute
                .builder().path(Arrays.asList("role", "perm")).reverse(true)
                .roleIdIn(Arrays.asList()).build();

        String sql = FieldProcessor.execute(field, argList, domainRoute);

        String expected = "id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN (null))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).isEmpty();
    }

    @Test
    void supportReverseNestedQueryWithThreeDomains() {
        DoytoDomainRoute domainRoute = DoytoDomainRoute
                .builder().path(Arrays.asList("user", "role", "perm")).reverse(true).build();

        String sql = FieldProcessor.execute(field, argList, domainRoute);

        String expected = "id IN (" +
                "SELECT permId FROM t_role_and_perm WHERE roleId IN (" +
                "SELECT roleId FROM t_user_and_role" +
                "))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).isEmpty();
    }

    @Test
    void supportReverseNestedQueryWithThreeDomainsAndSimpleConditionForLastDomain() {
        DoytoDomainRoute domainRoute = DoytoDomainRoute
                .builder().path(Arrays.asList("user", "role", "perm")).reverse(true)
                .userId(2).build();

        String sql = FieldProcessor.execute(field, argList, domainRoute);

        String expected = "id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN " +
                "(SELECT roleId FROM t_user_and_role WHERE userId = ?))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).containsExactly(2);
    }

    @Test
    void supportReverseNestedQueryWithThreeDomainsAndQueryForLastDomain() {
        UserQuery userQuery = UserQuery.builder().usernameLike("test").userLevel(UserLevel.普通).build();
        DoytoDomainRoute domainRoute = DoytoDomainRoute
                .builder().path(Arrays.asList("user", "role", "perm")).reverse(true)
                .userQuery(userQuery).build();

        String sql = FieldProcessor.execute(field, argList, domainRoute);

        String expected = "id IN (SELECT permId FROM t_role_and_perm WHERE roleId IN " +
                "(SELECT roleId FROM t_user_and_role WHERE userId IN " +
                "(SELECT id FROM t_user WHERE username LIKE ? AND userLevel = ?)))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).containsExactly("%test%", UserLevel.普通.ordinal());
    }

    @Test
    void supportReverseNestedQueryWithFourDomainsAndQueryForEachDomain() {
        DoytoDomainRoute domainRoute = DoytoDomainRoute
                .builder().path(Arrays.asList("user", "role", "perm", "menu")).reverse(true)
                .permQuery(PermissionQuery.builder().valid(true).build())
                .roleQuery(RoleQuery.builder().roleNameLike("vip").valid(true).build())
                .userId(1).build();

        String sql = FieldProcessor.execute(field, argList, domainRoute);

        String expected = "id IN (" +
                "SELECT menuId FROM t_perm_and_menu pm INNER JOIN t_perm p ON p.id = pm.permId AND p.valid = ? WHERE permId IN (" +
                "SELECT permId FROM t_role_and_perm rp INNER JOIN t_role r ON r.id = rp.roleId AND r.roleName LIKE ? AND r.valid = ? WHERE roleId IN (" +
                "SELECT roleId FROM t_user_and_role WHERE userId = ?)))";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).containsExactly(true, "%vip%", true, 1);
    }

    @Test
    void supportSelfOneToManyQuery() {
        MenuQuery parentQuery = MenuQuery.builder().nameLike("test").valid(true).build();
        DoytoDomainRoute domainRoute = DoytoDomainRoute
                .builder().path(Arrays.asList("menu")).lastDomainIdColumn("parent_id")
                .menuQuery(parentQuery)
                .build();

        String sql = FieldProcessor.execute(field, argList, domainRoute);

        String expected = "id IN (SELECT parent_id FROM t_menu WHERE name LIKE ? AND valid = ?)";
        assertThat(sql).isEqualTo(expected);
        assertThat(argList).containsExactly("%test%", true);
    }

}