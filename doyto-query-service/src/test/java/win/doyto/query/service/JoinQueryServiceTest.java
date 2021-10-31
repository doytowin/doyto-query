package win.doyto.query.service;

import org.junit.jupiter.api.Test;
import win.doyto.query.core.SqlAndArgs;
import win.doyto.query.core.test.TestEnum;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JoinQueryServiceTest
 *
 * @author f0rb on 2019-06-09
 */
class JoinQueryServiceTest {

    @Test
    void buildJoinSelectAndArgs() {
        JoinQueryService<TestJoinView, TestJoinQuery> JoinQueryService = new JoinQueryService<>(TestJoinView.class);

        TestJoinQuery testJoinQuery = new TestJoinQuery();
        testJoinQuery.setRoleName("VIP");
        testJoinQuery.setUserLevel(TestEnum.VIP);

        String expected = "SELECT username, r.roleName AS roleName " +
            "FROM user u " +
            "left join t_user_and_role ur on ur.userId = u.id " +
            "inner join role r on r.id = ur.roleId and r.roleName = ? " +
            "WHERE u.userLevel = ?";
        SqlAndArgs sqlAndArgs = JoinQueryService.buildJoinSelectAndArgs(testJoinQuery);
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("VIP", TestEnum.VIP.ordinal());
    }


    @Test
    void buildJoinSelectAndArgsWithAlias() {

        JoinQueryService<TestJoinView, TestJoinQuery> JoinQueryService = new JoinQueryService<>(TestJoinView.class);

        TestJoinQuery testJoinQuery = new TestJoinQuery();
        testJoinQuery.setRoleName("VIP");
        testJoinQuery.setRoleNameLikeOrRoleCodeLike("VIP");
        testJoinQuery.setUserLevel(TestEnum.VIP);

        String expected = "SELECT username, r.roleName AS roleName " +
            "FROM user u " +
            "left join t_user_and_role ur on ur.userId = u.id " +
            "inner join role r on r.id = ur.roleId and r.roleName = ? " +
            "WHERE u.userLevel = ? AND (r.roleName LIKE ? OR r.roleCode LIKE ?)";
        SqlAndArgs sqlAndArgs = JoinQueryService.buildJoinSelectAndArgs(testJoinQuery);
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("VIP", TestEnum.VIP.ordinal(), "%VIP%", "%VIP%");

    }

    @Test
    void buildJoinGroupBy() {
        JoinQueryService<UserCountByRoleView, TestJoinQuery> JoinQueryService
            = new JoinQueryService<>(UserCountByRoleView.class);

        TestJoinQuery testJoinQuery = new TestJoinQuery();
        testJoinQuery.setSort("userCount,asc").setPageSize(5);

        String expected = "SELECT r.roleName AS roleName, count(u.id) AS userCount " +
            "FROM user u " +
            "left join t_user_and_role ur on ur.userId = u.id " +
            "inner join role r on r.id = ur.roleId " +
            "GROUP BY r.roleName HAVING count(*) > 0 " +
            "ORDER BY userCount asc " +
            "LIMIT 5 OFFSET 0";
        SqlAndArgs sqlAndArgs = JoinQueryService.buildJoinSelectAndArgs(testJoinQuery);
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).isEmpty();
    }

}