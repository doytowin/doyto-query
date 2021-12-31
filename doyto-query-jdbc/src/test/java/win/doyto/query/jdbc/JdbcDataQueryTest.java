package win.doyto.query.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import win.doyto.query.service.PageList;
import win.doyto.query.test.join.TestJoinQuery;
import win.doyto.query.test.join.TestJoinView;
import win.doyto.query.test.join.UserCountByRoleView;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JdbcDataQueryTest
 *
 * @author f0rb on 2020-04-11
 */
class JdbcDataQueryTest extends JdbcApplicationTest {
    private JdbcDataQuery jdbcDataQuery;

    @BeforeEach
    void setUp(@Autowired JdbcOperations jdbcOperations) {
        jdbcDataQuery = new JdbcDataQuery(jdbcOperations);
    }

    @Test
    void queryForJoin() {
        TestJoinQuery query = new TestJoinQuery();
        query.setSort("userCount,desc");

        List<UserCountByRoleView> list = jdbcDataQuery.query(query, UserCountByRoleView.class);

        assertThat(list)
                .extracting(UserCountByRoleView::getUserCount)
                .containsExactly(3, 2);
    }

    @Test
    void countForGroupBy() {
        TestJoinQuery query = TestJoinQuery.builder().sort("userCount,desc").build();
        Long count = jdbcDataQuery.count(query, UserCountByRoleView.class);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void pageForJoin() {
        TestJoinQuery testJoinQuery = new TestJoinQuery();
        testJoinQuery.setRoleName("高级");

        PageList<TestJoinView> page = jdbcDataQuery.page(testJoinQuery, TestJoinView.class);

        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getList()).extracting(TestJoinView::getUsername).containsExactly("f0rb", "user4");
        assertThat(testJoinQuery.getPageNumber()).isZero();
        assertThat(testJoinQuery.getPageSize()).isEqualTo(10);
    }

}
