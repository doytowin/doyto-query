package win.doyto.query.demo.test;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.springframework.cache.support.NoOpCache;
import org.springframework.jdbc.core.JdbcOperations;
import win.doyto.query.cache.CacheWrapper;
import win.doyto.query.core.test.TestJoinQuery;
import win.doyto.query.core.test.TestJoinView;
import win.doyto.query.core.test.UserCountByRoleView;
import win.doyto.query.demo.module.role.RoleController;
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
public class JoinTest extends DemoApplicationTest {

    @Resource
    private JdbcOperations jdbcOperations;

    @Test
    void queryForJoin() {
        TestJoinQuery query = new TestJoinQuery();
        query.setSort("userCount,desc");

        List<UserCountByRoleView> list = new JoinQueryService<>(jdbcOperations, UserCountByRoleView.class).query(query);
        assertThat(list).extracting(UserCountByRoleView::getUserCount).containsExactly(3, 2);
    }

    @Test
    void pageForJoin() {
        TestJoinQuery testJoinQuery = new TestJoinQuery();
        testJoinQuery.setRoleName("高级");
        PageList<TestJoinView> page = new JoinQueryService<>(jdbcOperations, TestJoinView.class).page(testJoinQuery);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getList()).extracting(TestJoinView::getUsername).containsExactly("f0rb", "user4");
        assertThat(testJoinQuery.getPageNumber()).isEqualTo(0);
        assertThat(testJoinQuery.getPageSize()).isEqualTo(10);
    }

    /*=============== Cache ==================*/
    @Test
    void defaultNoCache() throws IllegalAccessException {
        RoleController roleController = wac.getBean(RoleController.class);
        CacheWrapper<?> entityCacheWrapper = (CacheWrapper<?>) FieldUtils.readField(roleController, "entityCacheWrapper", true);
        assertThat(entityCacheWrapper.getCache()).isInstanceOf(NoOpCache.class);
    }
}
