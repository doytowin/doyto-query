package win.doyto.query.jdbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import win.doyto.query.core.Pageable;
import win.doyto.query.service.PageList;
import win.doyto.query.test.join.TestJoinQuery;
import win.doyto.query.test.join.TestJoinView;
import win.doyto.query.test.join.UserCountByRoleView;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JoinTest
 *
 * @author f0rb on 2020-04-11
 */
class JoinTest extends JdbcApplicationTest {

    @Autowired
    private JdbcOperations jdbcOperations;

    @Test
    void queryForJoin() {
        ComplexQueryService<UserCountByRoleView, Pageable> complexQueryService = new ComplexQueryService<>(UserCountByRoleView.class);
        complexQueryService.setJdbcOperations(jdbcOperations);

        TestJoinQuery query = new TestJoinQuery();
        query.setSort("userCount,desc");

        List<UserCountByRoleView> list = complexQueryService.query(query);
        assertThat(list).extracting(UserCountByRoleView::getUserCount).containsExactly(3, 2);
    }

    @Test
    void pageForJoin() {
        ComplexQueryService<TestJoinView, Pageable> complexQueryService = new ComplexQueryService<>(TestJoinView.class);
        complexQueryService.setJdbcOperations(jdbcOperations);

        TestJoinQuery testJoinQuery = new TestJoinQuery();
        testJoinQuery.setRoleName("高级");
        PageList<TestJoinView> page = complexQueryService.page(testJoinQuery);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getList()).extracting(TestJoinView::getUsername).containsExactly("f0rb", "user4");
        assertThat(testJoinQuery.getPageNumber()).isZero();
        assertThat(testJoinQuery.getPageSize()).isEqualTo(10);
    }

}
