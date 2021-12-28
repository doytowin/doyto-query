/*
 * Copyright © 2019-2021 Forb Yuan
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

import org.junit.jupiter.api.Test;
import win.doyto.query.test.TestEnum;
import win.doyto.query.test.TestPageQuery;
import win.doyto.query.test.join.MaxIdView;
import win.doyto.query.test.join.TestJoinQuery;
import win.doyto.query.test.join.TestJoinView;
import win.doyto.query.test.join.UserCountByRoleView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JoinQueryBuilderTest
 *
 * @author f0rb on 2021-12-11
 */
class JoinQueryBuilderTest {
    @Test
    void supportAggregateQuery() {
        JoinQueryBuilder joinQueryBuilder = new JoinQueryBuilder(MaxIdView.class);
        SqlAndArgs sqlAndArgs = joinQueryBuilder.buildJoinSelectAndArgs(new TestPageQuery());
        assertEquals("SELECT max(id) AS maxId FROM user", sqlAndArgs.getSql());
    }

    @Test
    void buildJoinSelectAndArgs() {
        JoinQueryBuilder joinQueryBuilder = new JoinQueryBuilder(TestJoinView.class);

        TestJoinQuery testJoinQuery = new TestJoinQuery();
        testJoinQuery.setRoleName("VIP");
        testJoinQuery.setUserLevel(TestEnum.VIP);

        String expected = "SELECT username, r.roleName AS roleName " +
                "FROM user u " +
                "left join t_user_and_role ur on ur.userId = u.id " +
                "inner join t_role r on r.id = ur.roleId and r.roleName = ? " +
                "WHERE u.userLevel = ?";
        SqlAndArgs sqlAndArgs = joinQueryBuilder.buildJoinSelectAndArgs(testJoinQuery);
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("VIP", TestEnum.VIP.ordinal());
    }

    @Test
    void buildJoinSelectAndArgsWithAlias() {
        JoinQueryBuilder joinQueryBuilder = new JoinQueryBuilder(TestJoinView.class);

        TestJoinQuery testJoinQuery = new TestJoinQuery();
        testJoinQuery.setRoleName("VIP");
        testJoinQuery.setRoleNameLikeOrRoleCodeLike("VIP");
        testJoinQuery.setUserLevel(TestEnum.VIP);

        String expected = "SELECT username, r.roleName AS roleName " +
                "FROM user u " +
                "left join t_user_and_role ur on ur.userId = u.id " +
                "inner join t_role r on r.id = ur.roleId and r.roleName = ? " +
                "WHERE u.userLevel = ? AND (r.roleName LIKE ? OR r.roleCode LIKE ?)";
        SqlAndArgs sqlAndArgs = joinQueryBuilder.buildJoinSelectAndArgs(testJoinQuery);
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("VIP", TestEnum.VIP.ordinal(), "%VIP%", "%VIP%");

    }

    @Test
    void buildJoinGroupBy() {
        JoinQueryBuilder joinQueryBuilder = new JoinQueryBuilder(UserCountByRoleView.class);

        TestJoinQuery testJoinQuery = TestJoinQuery.builder().pageSize(5).sort("userCount,asc").build();

        String expected = "SELECT r.roleName AS roleName, count(u.id) AS userCount " +
                "FROM user u " +
                "left join t_user_and_role ur on ur.userId = u.id " +
                "inner join t_role r on r.id = ur.roleId " +
                "GROUP BY r.roleName HAVING count(*) > 0 " +
                "ORDER BY userCount asc " +
                "LIMIT 5 OFFSET 0";
        SqlAndArgs sqlAndArgs = joinQueryBuilder.buildJoinSelectAndArgs(testJoinQuery);
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).isEmpty();
    }
}