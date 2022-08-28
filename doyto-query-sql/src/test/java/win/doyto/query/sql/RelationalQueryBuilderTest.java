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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.PageQuery;
import win.doyto.query.test.PermissionQuery;
import win.doyto.query.test.UserQuery;
import win.doyto.query.test.join.*;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RelationalQueryBuilderTest
 *
 * @author f0rb on 2021-12-11
 */
@ResourceLock(value = "mapCamelCaseToUnderscore")
class RelationalQueryBuilderTest {
    @BeforeEach
    void setUp() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);
    }

    @AfterEach
    void tearDown() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);
    }

    @Test
    void supportAggregateQuery() {
        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(new PageQuery(), MaxIdView.class);
        assertEquals("SELECT max(id) AS maxId, first(createUserId) AS firstCreateUserId FROM user", sqlAndArgs.getSql());
    }

    @Test
    void buildSqlAndArgsForSubDomain() throws NoSuchFieldException {
        Field field = UserView.class.getDeclaredField("perms");

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                field, Arrays.asList(1, 2, 3), PermView.class);

        String expected = "\nSELECT ? AS PK_FOR_JOIN, id, permName, valid FROM t_perm\n" +
                " WHERE id IN (\n" +
                "  SELECT perm_id FROM j_role_and_perm WHERE role_id IN (\n" +
                "  SELECT role_id FROM j_user_and_role WHERE user_id = ?\n" +
                " ))\n" +
                "UNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, id, permName, valid FROM t_perm\n" +
                " WHERE id IN (\n" +
                "  SELECT perm_id FROM j_role_and_perm WHERE role_id IN (\n" +
                "  SELECT role_id FROM j_user_and_role WHERE user_id = ?\n" +
                " ))\n" +
                "UNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, id, permName, valid FROM t_perm\n" +
                " WHERE id IN (\n" +
                "  SELECT perm_id FROM j_role_and_perm WHERE role_id IN (\n" +
                "  SELECT role_id FROM j_user_and_role WHERE user_id = ?\n" +
                " ))";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 2, 2, 3, 3);
    }

    @Test
    void buildSqlAndArgsForSubDomain_FourDomains() throws NoSuchFieldException {
        Field field = UserView.class.getDeclaredField("menus");

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                field, Arrays.asList(1, 3), MenuView.class);

        String expected = "\nSELECT ? AS PK_FOR_JOIN, id, menuName, platform FROM t_menu\n" +
                " WHERE id IN (\n" +
                "  SELECT menu_id FROM j_perm_and_menu WHERE perm_id IN (\n" +
                "  SELECT perm_id FROM j_role_and_perm WHERE role_id IN (\n" +
                "  SELECT role_id FROM j_user_and_role WHERE user_id = ?\n" +
                " )))\n" +
                "UNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, id, menuName, platform FROM t_menu\n" +
                " WHERE id IN (\n" +
                "  SELECT menu_id FROM j_perm_and_menu WHERE perm_id IN (\n" +
                "  SELECT perm_id FROM j_role_and_perm WHERE role_id IN (\n" +
                "  SELECT role_id FROM j_user_and_role WHERE user_id = ?\n" +
                " )))";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 3, 3);
    }

    @Test
    void buildJoinSqlForReversePath_TwoDomains() throws NoSuchFieldException {
        Field field = RoleView.class.getDeclaredField("users");

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                field, Arrays.asList(1, 3), UserView.class);

        String expected = "\nSELECT ? AS PK_FOR_JOIN, id, username, email FROM t_user\n" +
                " WHERE id IN (\n" +
                "  SELECT user_id FROM j_user_and_role WHERE role_id = ?\n" +
                " )\n" +
                "UNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, id, username, email FROM t_user\n" +
                " WHERE id IN (\n" +
                "  SELECT user_id FROM j_user_and_role WHERE role_id = ?\n" +
                " )";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 3, 3);
    }

    @Test
    void buildJoinSqlForReversePath_ThreeDomains() throws NoSuchFieldException {
        Field field = PermView.class.getDeclaredField("users");

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                field, Arrays.asList(1, 3), UserView.class);

        String expected = "\n" +
                "SELECT ? AS PK_FOR_JOIN, id, username, email FROM t_user\n" +
                " WHERE id IN (\n" +
                "  SELECT user_id FROM j_user_and_role WHERE role_id IN (\n" +
                "  SELECT role_id FROM j_role_and_perm WHERE perm_id = ?\n" +
                " ))\n" +
                "UNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, id, username, email FROM t_user\n" +
                " WHERE id IN (\n" +
                "  SELECT user_id FROM j_user_and_role WHERE role_id IN (\n" +
                "  SELECT role_id FROM j_role_and_perm WHERE perm_id = ?\n" +
                " ))";
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 3, 3);
    }

    @Test
    void buildJoinSqlForReversePath_FourDomains() throws NoSuchFieldException {
        Field field = MenuView.class.getDeclaredField("users");

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                field, Arrays.asList(1, 3, 4), UserView.class);

        String expected = "\n" +
                "SELECT ? AS PK_FOR_JOIN, id, username, email FROM t_user\n" +
                " WHERE id IN (\n" +
                "  SELECT user_id FROM j_user_and_role WHERE role_id IN (\n" +
                "  SELECT role_id FROM j_role_and_perm WHERE perm_id IN (\n" +
                "  SELECT perm_id FROM j_perm_and_menu WHERE menu_id = ?\n" +
                " )))\n" +
                "UNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, id, username, email FROM t_user\n" +
                " WHERE id IN (\n" +
                "  SELECT user_id FROM j_user_and_role WHERE role_id IN (\n" +
                "  SELECT role_id FROM j_role_and_perm WHERE perm_id IN (\n" +
                "  SELECT perm_id FROM j_perm_and_menu WHERE menu_id = ?\n" +
                " )))\n" +
                "UNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, id, username, email FROM t_user\n" +
                " WHERE id IN (\n" +
                "  SELECT user_id FROM j_user_and_role WHERE role_id IN (\n" +
                "  SELECT role_id FROM j_role_and_perm WHERE perm_id IN (\n" +
                "  SELECT perm_id FROM j_perm_and_menu WHERE menu_id = ?\n" +
                " )))";
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 3, 3, 4, 4);
    }

    @Test
    void buildReverseJoinWithQuery() throws NoSuchFieldException {
        Field field = RoleView.class.getDeclaredField("users");

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                UserQuery.builder().emailLike("@163")
                         .pageSize(10).sort("id,DESC")
                         .build(),
                UserView.class, field, Arrays.asList(1, 2, 3)
        );

        String expected = "\n" +
                "SELECT ? AS PK_FOR_JOIN, id, username, email FROM t_user\n" +
                " WHERE id IN (\n" +
                "  SELECT user_id FROM j_user_and_role WHERE role_id = ?\n" +
                " ) AND email LIKE ?\n" +
                " ORDER BY id DESC LIMIT 10 OFFSET 0\n" +
                "UNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, id, username, email FROM t_user\n" +
                " WHERE id IN (\n" +
                "  SELECT user_id FROM j_user_and_role WHERE role_id = ?\n" +
                " ) AND email LIKE ?\n" +
                " ORDER BY id DESC LIMIT 10 OFFSET 0\n" +
                "UNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, id, username, email FROM t_user\n" +
                " WHERE id IN (\n" +
                "  SELECT user_id FROM j_user_and_role WHERE role_id = ?\n" +
                " ) AND email LIKE ?\n" +
                " ORDER BY id DESC LIMIT 10 OFFSET 0";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, "%@163%", 2, 2, "%@163%", 3, 3, "%@163%");
    }

    /**
     * Fetch at most 10 valid permissions for each user row,
     * and sorted by id in descending order.
     */
    @Test
    void buildSqlAndArgsForSubDomainWithQuery() throws NoSuchFieldException {
        Field field = UserView.class.getDeclaredField("perms");

        PermissionQuery permissionQuery = PermissionQuery.builder().valid(true)
                                                         .pageSize(10).sort("id,DESC").build();
        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                permissionQuery, PermView.class, field, Arrays.asList(1, 2, 3));

        String expected = "\nSELECT ? AS PK_FOR_JOIN, id, permName, valid FROM t_perm\n" +
                " WHERE id IN (\n" +
                "  SELECT perm_id FROM j_role_and_perm WHERE role_id IN (\n" +
                "  SELECT role_id FROM j_user_and_role WHERE user_id = ?\n" +
                " )) AND valid = ?\n" +
                " ORDER BY id DESC LIMIT 10 OFFSET 0\n" +
                "UNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, id, permName, valid FROM t_perm\n" +
                " WHERE id IN (\n" +
                "  SELECT perm_id FROM j_role_and_perm WHERE role_id IN (\n" +
                "  SELECT role_id FROM j_user_and_role WHERE user_id = ?\n" +
                " )) AND valid = ?\n" +
                " ORDER BY id DESC LIMIT 10 OFFSET 0\n" +
                "UNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, id, permName, valid FROM t_perm\n" +
                " WHERE id IN (\n" +
                "  SELECT perm_id FROM j_role_and_perm WHERE role_id IN (\n" +
                "  SELECT role_id FROM j_user_and_role WHERE user_id = ?\n" +
                " )) AND valid = ?\n" +
                " ORDER BY id DESC LIMIT 10 OFFSET 0";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, true, 2, 2, true, 3, 3, true);
    }

    @Test
    void buildSqlAndArgsForManyToOne() throws NoSuchFieldException {
        Field field = RoleView.class.getDeclaredField("createUser");

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                new UserQuery(), UserView.class, field, Arrays.asList(1, 3));

        String expected = "\nSELECT ? AS PK_FOR_JOIN, id, username, email FROM t_user\n" +
                " WHERE id = (\n" +
                "  SELECT create_user_id FROM t_role WHERE id = ?\n" +
                " )\n" +
                "UNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, id, username, email FROM t_user\n" +
                " WHERE id = (\n" +
                "  SELECT create_user_id FROM t_role WHERE id = ?\n" +
                " )";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 3, 3);
    }

    @Test
    void buildSqlAndArgsForOneToMany() throws NoSuchFieldException {
        Field field = UserView.class.getDeclaredField("createRoles");

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                new UserQuery(), RoleView.class, field, Arrays.asList(1, 3));

        String expected = "\n" +
                "SELECT ? AS PK_FOR_JOIN, id, roleName, roleCode, valid FROM t_role WHERE create_user_id = ?" +
                "\nUNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, id, roleName, roleCode, valid FROM t_role WHERE create_user_id = ?";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 3, 3);
    }

    @Test
    void supportHaving() {
        UserLevelHaving having = UserLevelHaving.builder().countGt(1).countLt(10).build();
        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSelectAndArgs(
                UserLevelQuery.builder().having(having).valid(true).build(), UserLevelCountView.class);

        String expected = "SELECT userLevel, valid, count(*) AS count FROM t_user WHERE valid = ? " +
                "GROUP BY userLevel, valid HAVING count(*) > ? AND count(*) < ?";

        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(true, 1, 10);
    }

    @Test
    void buildSqlAndArgsForManyToManyAggregation() throws NoSuchFieldException {
        Field field = UserView.class.getDeclaredField("roleStat");

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                field, Arrays.asList(1, 2, 3), RoleStatView.class);

        String expected = "\nSELECT ? AS PK_FOR_JOIN, count(*) AS count FROM t_role\n" +
                " WHERE id IN (\n" +
                "  SELECT role_id FROM j_user_and_role WHERE user_id = ?\n" +
                " )\n" +
                "UNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, count(*) AS count FROM t_role\n" +
                " WHERE id IN (\n" +
                "  SELECT role_id FROM j_user_and_role WHERE user_id = ?\n" +
                " )\n" +
                "UNION ALL\n" +
                "SELECT ? AS PK_FOR_JOIN, count(*) AS count FROM t_role\n" +
                " WHERE id IN (\n" +
                "  SELECT role_id FROM j_user_and_role WHERE user_id = ?\n" +
                " )";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 2, 2, 3, 3);
    }

}