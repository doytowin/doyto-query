package win.doyto.query.jdbc;

import org.junit.jupiter.api.Test;
import win.doyto.query.sql.SqlAndArgs;
import win.doyto.query.test.TestEnum;
import win.doyto.query.test.join.TestJoinQuery;
import win.doyto.query.test.join.TestJoinView;
import win.doyto.query.test.join.UserCountByRoleView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JoinQueryServiceTest
 *
 * @author f0rb on 2019-06-09
 */
class ComplexQueryServiceTest {

    @Test
    void buildJoinSelectAndArgs() {
        ComplexQueryService<TestJoinView, TestJoinQuery> complexQueryService = new ComplexQueryService<>(TestJoinView.class);

        TestJoinQuery testJoinQuery = new TestJoinQuery();
        testJoinQuery.setRoleName("VIP");
        testJoinQuery.setUserLevel(TestEnum.VIP);

        String expected = "SELECT username, r.roleName AS roleName " +
            "FROM user u " +
            "left join t_user_and_role ur on ur.userId = u.id " +
            "inner join role r on r.id = ur.roleId and r.roleName = ? " +
            "WHERE u.userLevel = ?";
        SqlAndArgs sqlAndArgs = complexQueryService.buildJoinSelectAndArgs(testJoinQuery);
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("VIP", TestEnum.VIP.ordinal());
    }


    @Test
    void buildJoinSelectAndArgsWithAlias() {

        ComplexQueryService<TestJoinView, TestJoinQuery> ComplexQueryService = new ComplexQueryService<>(TestJoinView.class);

        TestJoinQuery testJoinQuery = new TestJoinQuery();
        testJoinQuery.setRoleName("VIP");
        testJoinQuery.setRoleNameLikeOrRoleCodeLike("VIP");
        testJoinQuery.setUserLevel(TestEnum.VIP);

        String expected = "SELECT username, r.roleName AS roleName " +
            "FROM user u " +
            "left join t_user_and_role ur on ur.userId = u.id " +
            "inner join role r on r.id = ur.roleId and r.roleName = ? " +
            "WHERE u.userLevel = ? AND (r.roleName LIKE ? OR r.roleCode LIKE ?)";
        SqlAndArgs sqlAndArgs = ComplexQueryService.buildJoinSelectAndArgs(testJoinQuery);
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("VIP", TestEnum.VIP.ordinal(), "%VIP%", "%VIP%");

    }

    @Test
    void buildJoinGroupBy() {
        ComplexQueryService<UserCountByRoleView, TestJoinQuery> ComplexQueryService
            = new ComplexQueryService<>(UserCountByRoleView.class);

        TestJoinQuery testJoinQuery = TestJoinQuery.builder().pageSize(5).sort("userCount,asc").build();

        String expected = "SELECT r.roleName AS roleName, count(u.id) AS userCount " +
            "FROM user u " +
            "left join t_user_and_role ur on ur.userId = u.id " +
            "inner join role r on r.id = ur.roleId " +
            "GROUP BY r.roleName HAVING count(*) > 0 " +
            "ORDER BY userCount asc " +
            "LIMIT 5 OFFSET 0";
        SqlAndArgs sqlAndArgs = ComplexQueryService.buildJoinSelectAndArgs(testJoinQuery);
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).isEmpty();
    }

}