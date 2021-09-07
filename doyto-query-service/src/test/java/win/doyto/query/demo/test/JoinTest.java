package win.doyto.query.demo.test;

import org.junit.jupiter.api.Test;
import win.doyto.query.core.test.TestJoinQuery;
import win.doyto.query.core.test.TestJoinView;
import win.doyto.query.core.test.UserCountByRoleView;
import win.doyto.query.data.DatabaseOperations;
import win.doyto.query.service.JoinQueryService;
import win.doyto.query.service.PageList;

import java.util.List;
import javax.annotation.Resource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JoinTest
 *
 * @author f0rb on 2020-04-11
 */
class JoinTest extends DemoApplicationTest {

    @Resource
    private DatabaseOperations dataOperations;

    @Test
    void queryForJoin() {
        TestJoinQuery query = new TestJoinQuery();
        query.setSort("userCount,desc");

        List<UserCountByRoleView> list = new JoinQueryService<>(dataOperations, UserCountByRoleView.class).query(query);
        assertThat(list).extracting(UserCountByRoleView::getUserCount).containsExactly(3, 2);
    }

    @Test
    void pageForJoin() {
        TestJoinQuery testJoinQuery = new TestJoinQuery();
        testJoinQuery.setRoleName("高级");
        PageList<TestJoinView> page = new JoinQueryService<>(dataOperations, TestJoinView.class).page(testJoinQuery);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getList()).extracting(TestJoinView::getUsername).containsExactly("f0rb", "user4");
        assertThat(testJoinQuery.getPageNumber()).isZero();
        assertThat(testJoinQuery.getPageSize()).isEqualTo(10);
    }

}
