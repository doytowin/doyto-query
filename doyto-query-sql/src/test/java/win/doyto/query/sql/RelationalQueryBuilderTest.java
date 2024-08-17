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

package win.doyto.query.sql;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.PageQuery;
import win.doyto.query.test.menu.MenuView;
import win.doyto.query.test.perm.PermView;
import win.doyto.query.test.perm.PermissionQuery;
import win.doyto.query.test.role.RoleQuery;
import win.doyto.query.test.role.RoleStatView;
import win.doyto.query.test.role.RoleView;
import win.doyto.query.test.user.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RelationalQueryBuilderTest
 *
 * @author f0rb on 2021-12-11
 */
class RelationalQueryBuilderTest {
    static <I extends Serializable, R> SqlAndArgs buildSqlAndArgsForSubDomain(Field joinField, List<I> mainIds, Class<R> joinEntityClass) {
        return RelationalQueryBuilder.buildSqlAndArgsForSubDomain(new PageQuery(), joinEntityClass, joinField, mainIds);
    }

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
        assertEquals("SELECT max(id) AS maxId, first(createUserId) AS firstCreateUserId FROM t_user", sqlAndArgs.getSql());
    }

    @Test
    void buildSqlAndArgsForSubDomain_ThreeLevels() throws NoSuchFieldException {
        Field field = UserEntity.class.getDeclaredField("perms");

        SqlAndArgs sqlAndArgs = buildSqlAndArgsForSubDomain(field, Arrays.asList(1, 2, 3), PermView.class);

        String expected = """

                SELECT ? AS MAIN_ENTITY_ID, id, permName, valid FROM t_perm
                WHERE id IN (
                  SELECT perm_id FROM a_role_and_perm WHERE role_id IN (
                  SELECT role_id FROM a_user_and_role WHERE user_id = ?
                ))
                UNION ALL
                SELECT ? AS MAIN_ENTITY_ID, id, permName, valid FROM t_perm
                WHERE id IN (
                  SELECT perm_id FROM a_role_and_perm WHERE role_id IN (
                  SELECT role_id FROM a_user_and_role WHERE user_id = ?
                ))
                UNION ALL
                SELECT ? AS MAIN_ENTITY_ID, id, permName, valid FROM t_perm
                WHERE id IN (
                  SELECT perm_id FROM a_role_and_perm WHERE role_id IN (
                  SELECT role_id FROM a_user_and_role WHERE user_id = ?
                ))""";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 2, 2, 3, 3);
    }

    @Test
    void buildSqlAndArgsForSubDomain_FourLevels() throws NoSuchFieldException {
        Field field = UserEntity.class.getDeclaredField("menus");

        SqlAndArgs sqlAndArgs = buildSqlAndArgsForSubDomain(field, Arrays.asList(1, 3), MenuView.class);

        String expected = """

                SELECT ? AS MAIN_ENTITY_ID, id, menuName, platform FROM t_menu
                WHERE id IN (
                  SELECT menu_id FROM a_perm_and_menu WHERE perm_id IN (
                  SELECT perm_id FROM a_role_and_perm WHERE role_id IN (
                  SELECT role_id FROM a_user_and_role WHERE user_id = ?
                )))
                UNION ALL
                SELECT ? AS MAIN_ENTITY_ID, id, menuName, platform FROM t_menu
                WHERE id IN (
                  SELECT menu_id FROM a_perm_and_menu WHERE perm_id IN (
                  SELECT perm_id FROM a_role_and_perm WHERE role_id IN (
                  SELECT role_id FROM a_user_and_role WHERE user_id = ?
                )))""";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 3, 3);
    }

    @Test
    void buildJoinSqlForReversePath_TwoLevels() throws NoSuchFieldException {
        Field field = RoleView.class.getDeclaredField("users");

        SqlAndArgs sqlAndArgs = buildSqlAndArgsForSubDomain(field, Arrays.asList(1, 3), UserEntity.class);

        String expected = """

                SELECT ? AS MAIN_ENTITY_ID, id, username, email FROM t_user
                WHERE id IN (
                  SELECT user_id FROM a_user_and_role WHERE role_id = ?
                )
                UNION ALL
                SELECT ? AS MAIN_ENTITY_ID, id, username, email FROM t_user
                WHERE id IN (
                  SELECT user_id FROM a_user_and_role WHERE role_id = ?
                )""";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 3, 3);
    }

    @Test
    void buildJoinSqlForReversePath_ThreeLevels() throws NoSuchFieldException {
        Field field = PermView.class.getDeclaredField("users");

        SqlAndArgs sqlAndArgs = buildSqlAndArgsForSubDomain(field, Arrays.asList(1, 3), UserEntity.class);

        String expected = """

                SELECT ? AS MAIN_ENTITY_ID, id, username, email FROM t_user
                WHERE id IN (
                  SELECT user_id FROM a_user_and_role WHERE role_id IN (
                  SELECT role_id FROM a_role_and_perm WHERE perm_id = ?
                ))
                UNION ALL
                SELECT ? AS MAIN_ENTITY_ID, id, username, email FROM t_user
                WHERE id IN (
                  SELECT user_id FROM a_user_and_role WHERE role_id IN (
                  SELECT role_id FROM a_role_and_perm WHERE perm_id = ?
                ))""";
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 3, 3);
    }

    @Test
    void buildJoinSqlForReversePath_FourLevels() throws NoSuchFieldException {
        Field field = MenuView.class.getDeclaredField("users");

        SqlAndArgs sqlAndArgs = buildSqlAndArgsForSubDomain(field, Arrays.asList(1, 3, 4), UserEntity.class);

        String expected = """

                SELECT ? AS MAIN_ENTITY_ID, id, username, email FROM t_user
                WHERE id IN (
                  SELECT user_id FROM a_user_and_role WHERE role_id IN (
                  SELECT role_id FROM a_role_and_perm WHERE perm_id IN (
                  SELECT perm_id FROM a_perm_and_menu WHERE menu_id = ?
                )))
                UNION ALL
                SELECT ? AS MAIN_ENTITY_ID, id, username, email FROM t_user
                WHERE id IN (
                  SELECT user_id FROM a_user_and_role WHERE role_id IN (
                  SELECT role_id FROM a_role_and_perm WHERE perm_id IN (
                  SELECT perm_id FROM a_perm_and_menu WHERE menu_id = ?
                )))
                UNION ALL
                SELECT ? AS MAIN_ENTITY_ID, id, username, email FROM t_user
                WHERE id IN (
                  SELECT user_id FROM a_user_and_role WHERE role_id IN (
                  SELECT role_id FROM a_role_and_perm WHERE perm_id IN (
                  SELECT perm_id FROM a_perm_and_menu WHERE menu_id = ?
                )))""";
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
                UserEntity.class, field, Arrays.asList(1, 2, 3)
        );

        String expected = """

                (SELECT ? AS MAIN_ENTITY_ID, id, username, email FROM t_user
                WHERE id IN (
                  SELECT user_id FROM a_user_and_role WHERE role_id = ?
                ) AND email LIKE ?
                ORDER BY id DESC LIMIT 10 OFFSET 0)
                UNION ALL
                (SELECT ? AS MAIN_ENTITY_ID, id, username, email FROM t_user
                WHERE id IN (
                  SELECT user_id FROM a_user_and_role WHERE role_id = ?
                ) AND email LIKE ?
                ORDER BY id DESC LIMIT 10 OFFSET 0)
                UNION ALL
                (SELECT ? AS MAIN_ENTITY_ID, id, username, email FROM t_user
                WHERE id IN (
                  SELECT user_id FROM a_user_and_role WHERE role_id = ?
                ) AND email LIKE ?
                ORDER BY id DESC LIMIT 10 OFFSET 0)""";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, "%@163%", 2, 2, "%@163%", 3, 3, "%@163%");
    }

    /**
     * Fetch at most 10 valid permissions for each user row,
     * and sorted by id in descending order.
     */
    @Test
    void buildSqlAndArgsForSubDomainWithQuery() throws NoSuchFieldException {
        Field field = UserEntity.class.getDeclaredField("perms");

        PermissionQuery permissionQuery = PermissionQuery.builder().valid(true)
                                                         .pageSize(10).sort("id,DESC").build();
        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                permissionQuery, PermView.class, field, Arrays.asList(1, 2, 3));

        String expected = """

                (SELECT ? AS MAIN_ENTITY_ID, id, permName, valid FROM t_perm
                WHERE id IN (
                  SELECT perm_id FROM a_role_and_perm WHERE role_id IN (
                  SELECT role_id FROM a_user_and_role WHERE user_id = ?
                )) AND valid = ?
                ORDER BY id DESC LIMIT 10 OFFSET 0)
                UNION ALL
                (SELECT ? AS MAIN_ENTITY_ID, id, permName, valid FROM t_perm
                WHERE id IN (
                  SELECT perm_id FROM a_role_and_perm WHERE role_id IN (
                  SELECT role_id FROM a_user_and_role WHERE user_id = ?
                )) AND valid = ?
                ORDER BY id DESC LIMIT 10 OFFSET 0)
                UNION ALL
                (SELECT ? AS MAIN_ENTITY_ID, id, permName, valid FROM t_perm
                WHERE id IN (
                  SELECT perm_id FROM a_role_and_perm WHERE role_id IN (
                  SELECT role_id FROM a_user_and_role WHERE user_id = ?
                )) AND valid = ?
                ORDER BY id DESC LIMIT 10 OFFSET 0)""";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, true, 2, 2, true, 3, 3, true);
    }

    @Test
    void buildSqlAndArgsForManyToOne() throws NoSuchFieldException {
        Field field = RoleView.class.getDeclaredField("createUser");

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                new UserQuery(), UserEntity.class, field, Arrays.asList(1, 3));

        String expected = """

                SELECT ? AS MAIN_ENTITY_ID, id, username, email FROM t_user
                WHERE id IN (
                  SELECT createUserId FROM t_role WHERE id = ?
                )
                UNION ALL
                SELECT ? AS MAIN_ENTITY_ID, id, username, email FROM t_user
                WHERE id IN (
                  SELECT createUserId FROM t_role WHERE id = ?
                )""";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 3, 3);
    }

    @Test
    void buildSqlAndArgsForManyToOneWithConditionsAndOrderByAndPaging() throws NoSuchFieldException {
        Field field = RoleView.class.getDeclaredField("createUser");

        UserQuery userQuery = UserQuery.builder().memoNull(true).sort("id,desc").pageSize(5).build();
        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                userQuery, UserEntity.class, field, Arrays.asList(1, 3));

        String expected = """

                (SELECT ? AS MAIN_ENTITY_ID, id, username, email FROM t_user
                WHERE id IN (
                  SELECT createUserId FROM t_role WHERE id = ?
                ) AND memo IS NULL
                ORDER BY id desc LIMIT 5 OFFSET 0)
                UNION ALL
                (SELECT ? AS MAIN_ENTITY_ID, id, username, email FROM t_user
                WHERE id IN (
                  SELECT createUserId FROM t_role WHERE id = ?
                ) AND memo IS NULL
                ORDER BY id desc LIMIT 5 OFFSET 0)""";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 3, 3);
    }

    @Test
    void buildSqlAndArgsForOneToMany() throws NoSuchFieldException {
        Field field = UserEntity.class.getDeclaredField("createRoles");

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                new UserQuery(), RoleView.class, field, Arrays.asList(1, 3));

        String expected = """

                SELECT ? AS MAIN_ENTITY_ID, id, roleName, roleCode, valid FROM t_role
                WHERE createUserId = ?
                UNION ALL
                SELECT ? AS MAIN_ENTITY_ID, id, roleName, roleCode, valid FROM t_role
                WHERE createUserId = ?""";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 3, 3);
    }

    @Test
    void buildSqlAndArgsForOneToManyWithConditionsAndOrderByAndPaging() throws NoSuchFieldException {
        Field field = UserEntity.class.getDeclaredField("createRoles");

        RoleQuery roleQuery = RoleQuery.builder().valid(true).sort("id,desc").pageSize(5).build();
        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildSqlAndArgsForSubDomain(
                roleQuery, RoleView.class, field, Arrays.asList(1, 3));

        String expected = """

                (SELECT ? AS MAIN_ENTITY_ID, id, roleName, roleCode, valid FROM t_role
                WHERE createUserId = ? AND valid = ?
                ORDER BY id desc LIMIT 5 OFFSET 0)
                UNION ALL
                (SELECT ? AS MAIN_ENTITY_ID, id, roleName, roleCode, valid FROM t_role
                WHERE createUserId = ? AND valid = ?
                ORDER BY id desc LIMIT 5 OFFSET 0)""";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, true, 3, 3, true);
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
        Field field = UserEntity.class.getDeclaredField("roleStat");

        SqlAndArgs sqlAndArgs = buildSqlAndArgsForSubDomain(field, Arrays.asList(1, 2, 3), RoleStatView.class);

        String expected = """

                SELECT ? AS MAIN_ENTITY_ID, count(*) AS count FROM t_role
                WHERE id IN (
                  SELECT role_id FROM a_user_and_role WHERE user_id = ?
                )
                UNION ALL
                SELECT ? AS MAIN_ENTITY_ID, count(*) AS count FROM t_role
                WHERE id IN (
                  SELECT role_id FROM a_user_and_role WHERE user_id = ?
                )
                UNION ALL
                SELECT ? AS MAIN_ENTITY_ID, count(*) AS count FROM t_role
                WHERE id IN (
                  SELECT role_id FROM a_user_and_role WHERE user_id = ?
                )""";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1, 1, 2, 2, 3, 3);
    }

    @Test
    void buildCountAndArgs() {
        UserQuery testJoinQuery = UserQuery.builder().memoNull(true).pageSize(5).sort("userLevel,asc").build();

        SqlAndArgs sqlAndArgs = RelationalQueryBuilder.buildCountAndArgs(testJoinQuery, UserLevelCountView.class);

        String expected = "SELECT COUNT(DISTINCT(userLevel, valid)) FROM t_user WHERE memo IS NULL";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
    }

}