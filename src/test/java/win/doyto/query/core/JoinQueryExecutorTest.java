package win.doyto.query.core;

import org.junit.jupiter.api.Test;
import win.doyto.query.core.test.TestEnum;
import win.doyto.query.core.test.TestJoinQuery;
import win.doyto.query.core.test.TestJoinView;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JoinQueryExecutorTest
 *
 * @author f0rb on 2019-06-09
 */
class JoinQueryExecutorTest {

    @Test
    void buildJoinSelectAndArgs() {
        JoinQueryExecutor<TestJoinView, TestJoinQuery> joinQueryExecutor = new JoinQueryExecutor<>(null, TestJoinView.class);

        TestJoinQuery testJoinQuery = new TestJoinQuery();
        testJoinQuery.setRoleName("VIP");
        testJoinQuery.setUserLevel(TestEnum.VIP);

        String expected = "SELECT u.username AS username, r.roleName AS roleName " +
            "FROM user u " +
            "left join user_and_role ur on ur.userId = u.id " +
            "left join role r on r.id = ur.roleId and r.roleName = ? " +
            "WHERE userLevel = ?";
        SqlAndArgs sqlAndArgs = joinQueryExecutor.buildJoinSelectAndArgs(testJoinQuery);
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("VIP", TestEnum.VIP.ordinal());
    }

}